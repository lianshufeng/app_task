package top.dzurl.apptask.core.runtime.impl;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.dzurl.apptask.core.appium.AppiumServer;
import top.dzurl.apptask.core.conf.AppTaskConf;
import top.dzurl.apptask.core.model.AndroidDeviceInfo;
import top.dzurl.apptask.core.model.ScriptRuntime;
import top.dzurl.apptask.core.model.runtime.AndroidMachineScriptRuntime;
import top.dzurl.apptask.core.runtime.RunTimeEnvironmentManager;
import top.dzurl.apptask.core.runtime.model.AndroidMachineDevice;
import top.dzurl.apptask.core.util.ADBUtil;
import top.dzurl.apptask.core.util.BeanUtil;
import top.dzurl.apptask.core.util.LeiDianSimulatorUtil;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * android真机
 */
@Slf4j
@Component
public class AndroidMachineRunTimeManager implements RunTimeEnvironmentManager {

    //正在运行的虚拟机
    private Vector<RunningMachine> runningSimulators = new Vector<>();

    //设备mac的缓存
    private Map<String, String> deviceMacCache = new ConcurrentHashMap<>();

    @Autowired
    private AppTaskConf appTaskConf;

    @Autowired
    private AppiumServer appiumServer;

    private File ADBHome = null;

    @Autowired
    private void init(ApplicationContext applicationContext) {
        ADBHome = appTaskConf.getRunTime().getADBHome();
    }

    @Override
    public synchronized void open(ScriptRuntime runtime) {
        final AndroidMachineScriptRuntime scriptRuntime = (AndroidMachineScriptRuntime) runtime;

        RunningMachine runningMachine = findMachine(runtime);
        if (runningMachine == null) {
            throw new RuntimeException("没有可用的android设备");
        }

        //赋值驱动
        scriptRuntime.setDriver(runningMachine.getDriver());
        scriptRuntime.setDeviceName(runningMachine.getDeviceName());


    }

    @Override
    public void close(ScriptRuntime runtime) {
        final AndroidMachineScriptRuntime scriptRuntime = (AndroidMachineScriptRuntime) runtime;
        //关闭android真机为完成
        this.runningSimulators.stream().filter((it) -> {
            return it.getDeviceName().equals(scriptRuntime.getDeviceName());
        }).forEach((it) -> {
            it.setWorking(false);
        });
        scriptRuntime.setDriver(null);
        scriptRuntime.setDeviceName(null);
    }


    /**
     * 寻找可以用的物理手机
     *
     * @return
     */
    private RunningMachine findMachine(ScriptRuntime runtime) {
        //优先通过内存中获取设备
        RunningMachine runningMachine = findMachineFromMem(runtime);
        if (runningMachine != null) {
            return runningMachine;
        }

        //取出系统中所有已经连接adb的设备
        Set<String> devices = ADBUtil.list(ADBHome);
        log.debug("取出所有的设备 :{} ", devices);

        //过滤正在执行的任务
        devices.removeAll(runningSimulators.stream().filter((it) -> {
            return it.working;
        }).map((it) -> {
            return it.getDeviceName();
        }).collect(Collectors.toSet()));
        log.debug("过滤运行的设备 : {}", devices);


        //取出模拟器列表所有的mac地址,B0C0E4629
        Set<String> simulatorMacs = LeiDianSimulatorUtil.list(this.appTaskConf.getRunTime().getSimulator().getHome()).values().stream().map((it) -> {
            Object mac = it.get("propertySettings.macAddress");
            return mac == null ? null : String.valueOf(mac).toLowerCase();
        }).filter((it) -> {
            return StringUtils.hasText(it);
        }).collect(Collectors.toSet());
        //过滤模拟器
        devices.removeAll(devices.stream().filter((it) -> {
            String mac = this.getMac(it);
            if (mac == null) {
                return true;
            }
            mac = mac.replaceAll(":", "").toLowerCase();
            return simulatorMacs.contains(mac);
        }).collect(Collectors.toSet()));
        log.debug("过滤模拟器 : {}", devices);


        //取出运行环境中的设备信息
        final AndroidMachineDevice machineDevice = (AndroidMachineDevice) runtime.getEnvironment().getDevice();

        for (String device : devices) {
            AndroidDeviceInfo androidDeviceInfo = ADBUtil.getInfo(ADBHome, device);
            if (matchDevice(androidDeviceInfo, machineDevice)) {
                runningMachine = buildRunningMachine(device, androidDeviceInfo);
                runningMachine.setWorking(true);
                this.runningSimulators.add(runningMachine);
                return runningMachine;
            }
        }

        return null;
    }


    /**
     * 取出mac地址，优先读缓存
     *
     * @param deviceName
     * @return
     */
    private String getMac(String deviceName) {
        String mac = this.deviceMacCache.get(deviceName);
        if (StringUtils.hasText(mac)) {
            return mac;
        }
        mac = ADBUtil.getMac(ADBHome, deviceName);
        this.deviceMacCache.put(deviceName, mac);
        return mac;
    }


    /**
     * 构建真机设备
     *
     * @return
     */
    private RunningMachine buildRunningMachine(String deviceName, AndroidDeviceInfo androidDeviceInfo) {
        RunningMachine runningMachine = new RunningMachine();

        //构建appium服务端
        runningMachine.setAppiumDriverLocalService(this.appiumServer.buildService());
        runningMachine.setDriver(this.appiumServer.buildAndroidDriver(runningMachine.getAppiumDriverLocalService(), deviceName));
        runningMachine.setDeviceName(deviceName);
        runningMachine.setAndroidDeviceInfo(androidDeviceInfo);

        return runningMachine;
    }

    /**
     * 匹配设备信息
     *
     * @param androidDeviceInfo
     * @param machineDevice
     * @return
     */
    private boolean matchDevice(AndroidDeviceInfo androidDeviceInfo, AndroidMachineDevice machineDevice) {
        Map<String, Object> info = BeanUtil.toMap(androidDeviceInfo);
        Map<String, Object> device = BeanUtil.toMap(machineDevice);

        String[] matchWords = new String[]{
                "version", "sdk", "productModel", "productBrand", "serialno", "mac"
        };

        for (String matchWord : matchWords) {
            if (!matchDeviceMap(info, device, matchWord)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 比较信息的某一项
     *
     * @param info
     * @param device
     * @param item
     * @return
     */
    private boolean matchDeviceMap(Map<String, Object> info, Map<String, Object> device, String item) {
        Object infoVal = info.get(item);
        Object deviceVal = device.get(item);
        if (infoVal == null || deviceVal == null) {
            return true;
        }
        return infoVal.equals(deviceVal);
    }


    /**
     * 通过内存中查找设备
     *
     * @return
     */
    private RunningMachine findMachineFromMem(ScriptRuntime runtime) {
        //可以选择列表
        List<RunningMachine> runningMachineList = this.runningSimulators.stream().filter((it) -> {
            return !it.working;
        }).collect(Collectors.toList());

        //取出运行环境中的设备信息
        final AndroidMachineDevice machineDevice = (AndroidMachineDevice) runtime.getEnvironment().getDevice();
        for (RunningMachine runningMachine : runningMachineList) {
            if (matchDevice(runningMachine.getAndroidDeviceInfo(), machineDevice)) {
                return runningMachine;
            }
        }
        return null;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RunningMachine {

        //设备名
        private String deviceName;

        //模拟器对应的本地服务
        private AppiumDriverLocalService appiumDriverLocalService;

        //android的驱动
        private AndroidDriver driver;

        //是否正在工作
        private boolean working;

        //android设备
        private AndroidDeviceInfo androidDeviceInfo;

    }

}

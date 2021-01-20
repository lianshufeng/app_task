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
import top.dzurl.apptask.core.runtime.RunTimeEnvironmentManager;
import top.dzurl.apptask.core.runtime.model.AndroidMachineDevice;
import top.dzurl.apptask.core.util.ADBUtil;
import top.dzurl.apptask.core.util.LeiDianSimulatorUtil;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * android真机
 */
@Slf4j
@Component
public class AndroidMachineRunTimeManager implements RunTimeEnvironmentManager {

    //正在运行的虚拟机
    private Vector<RunningMachine> runningSimulators = new Vector<>();

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
        RunningMachine runningMachine = findMachine(runtime);
        if (runningMachine == null) {
            throw new RuntimeException("没有可用的android设备");
        }
    }

    @Override
    public void close(ScriptRuntime runtime) {


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
            return it.adbConnectionName;
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
            String mac = ADBUtil.getMac(ADBHome, it);
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
                runningMachine = buildRunningMachine(runtime);
                runningMachine.setWorking(true);
                this.runningSimulators.add(runningMachine);
                return runningMachine;
            }
        }


        return null;
    }


    /**
     * 构建真机设备
     *
     * @return
     */
    private RunningMachine buildRunningMachine(ScriptRuntime runtime) {
        //todo 实例化运行设备，连接appium驱动等
        return null;
    }

    /**
     * 匹配设备信息
     *
     * @param androidDeviceInfo
     * @param machineDevice
     * @return
     */
    private boolean matchDevice(AndroidDeviceInfo androidDeviceInfo, AndroidMachineDevice machineDevice) {
        //todo 设备信息比较
        return false;
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

        //取出adb的androidid
        private String adbConnectionName;

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

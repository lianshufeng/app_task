package top.dzurl.apptask.core.runtime.impl;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.dzurl.apptask.core.appium.AppiumServer;
import top.dzurl.apptask.core.conf.AppTaskConf;
import top.dzurl.apptask.core.model.Environment;
import top.dzurl.apptask.core.model.ScriptRuntime;
import top.dzurl.apptask.core.model.runtime.AndroidSimulatorScriptRuntime;
import top.dzurl.apptask.core.runtime.RunTimeEnvironmentManager;
import top.dzurl.apptask.core.runtime.model.AndroidSimulatorDevice;
import top.dzurl.apptask.core.util.ADBUtil;
import top.dzurl.apptask.core.util.BeanUtil;
import top.dzurl.apptask.core.util.LeiDianSimulatorUtil;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 雷电模拟器
 */
@Slf4j
@Component
public class AndroidSimulatorRunTimeManager implements RunTimeEnvironmentManager {

    private static final String CustomName = "custom";

    //正在运行的虚拟机
    private Vector<RunningSimulator> runningSimulators = new Vector<>();

    //雷电模拟器的文件夹地址
    private File leiDianHome = null;

    @Autowired
    private AppTaskConf appTaskConf;

    @Autowired
    private AppiumServer appiumServer;


    @Autowired
    private void init(ApplicationContext applicationContext) {
        leiDianHome = appTaskConf.getRunTime().getSimulator().getHome();

        //监视进程退出
        listenShutdown();

        //重启adb服务
        restartADB();

    }


    /**
     * 监视退出
     */
    @SneakyThrows
    private void listenShutdown() {
        //关闭系统则释放资源
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("系统退出，释放模拟器 : {}", runningSimulators.stream().map((it) -> {
                return it.getSimulatorName();
            }).collect(Collectors.toSet()));


            //关闭模拟器
            runningSimulators.forEach((it) -> {
                closeSimulator(it, true, true, false);
            });

            //结束所有进程
            try {
                String runTimePath = FilenameUtils.normalize(appTaskConf.getRunTime().getHome().getAbsolutePath()).replaceAll("\\\\", "\\\\\\\\");
                String cmd = "wmic process where \"name!= 'java.exe' and ExecutablePath like '" + runTimePath + "%' \" delete";
                Runtime.getRuntime().exec("cmd /c " + cmd).waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }));
    }

    /**
     * 重启adb服务
     */
    private void restartADB() {
        ADBUtil.restartADB(leiDianHome);
    }


    @Override
    public synchronized void open(ScriptRuntime runtime) {
        final AndroidSimulatorScriptRuntime simulatorScriptRuntime = (AndroidSimulatorScriptRuntime) runtime;

        Environment runtimeEnvironment = simulatorScriptRuntime.getEnvironment();

        //查询内存中缓存的模拟器
        RunningSimulator simulator = findSimulatorFromCache(runtimeEnvironment);
        if (simulator == null) {
            simulator = loadDiskSimulator(simulatorScriptRuntime);
        }

        //设置运行环境的驱动与模拟器名称
        simulatorScriptRuntime.setDriver(simulator.getDriver());
        simulatorScriptRuntime.setSimulatorName(simulator.getSimulatorName());


    }


    /**
     * 载入磁盘上的虚拟机到内存
     */
    private RunningSimulator loadDiskSimulator(final ScriptRuntime runtime) {
        RunningSimulator runningSimulator = new RunningSimulator();

        //匹配合适的模拟器
        String simulatorName = findAndBuildSimulator(runtime.getEnvironment());
        runningSimulator.setSimulatorName(simulatorName);

        //启动模拟器,返回驱动
        String adbConnectionName = startSimulator(simulatorName);
        runningSimulator.setAdbConnectionName(adbConnectionName);

        //构建appuim的服务端
        log.info("[启动] - Appium服务");
        AppiumDriverLocalService appiumDriverLocalService = buildService();
        runningSimulator.setAppiumDriverLocalService(appiumDriverLocalService);


        //构建客户端,并连接
        log.info("[连接] - {} -> {}", simulatorName, appiumDriverLocalService.getUrl().toString());
        AndroidDriver driver = buildClient(appiumDriverLocalService, adbConnectionName);
        runningSimulator.setDriver(driver);


        //最后一次访问时间
        runningSimulator.setLastAccessTime(System.currentTimeMillis());
        runningSimulator.setWorking(true);

        //设置磁盘上数据
        runningSimulator.setInfo(LeiDianSimulatorUtil.get(leiDianHome, simulatorName));

        this.runningSimulators.add(runningSimulator);

        return runningSimulator;
    }

    /**
     * 释放模拟器
     *
     * @param runningSimulator
     */
    private void closeSimulator(RunningSimulator runningSimulator, boolean closeSession, boolean quitSimulator, boolean removeCache) {

        //关掉appium服务
        if (closeSession) {
            Optional.ofNullable(runningSimulator.getAppiumDriverLocalService()).ifPresent((it) -> {
                it.stop();
            });
            runningSimulator.setAppiumDriverLocalService(null);
            runningSimulator.setDriver(null);
        }

        //关闭模拟器
        if (quitSimulator) {
            LeiDianSimulatorUtil.quit(leiDianHome, runningSimulator.simulatorName);
        }

        //删除内存对象
        if (removeCache) {
            this.runningSimulators.remove(runningSimulator);
        }

        log.info("releaseSimulator : {}", runningSimulator.getSimulatorName());
    }


    @Override
    public void close(ScriptRuntime runtime) {
        final AndroidSimulatorScriptRuntime simulatorScriptRuntime = (AndroidSimulatorScriptRuntime) runtime;
        //标记模拟器未工作
        Optional.ofNullable(findRunningSimulatorByName(simulatorScriptRuntime.getSimulatorName())).ifPresent((it) -> {
            it.setLastAccessTime(System.currentTimeMillis());
            it.setWorking(false);
        });


        //释放驱动
        simulatorScriptRuntime.setDriver(null);
        simulatorScriptRuntime.setSimulatorName(null);
    }


    /**
     * 启动模拟器
     */
    private String startSimulator(String simulatorName) {
        //启动虚拟机
        log.info("[启动] - 模拟器: {}", simulatorName);
        LeiDianSimulatorUtil.launch(leiDianHome, simulatorName);

        //取出adb的连接名
        String adbConnectionName = waitAndGetAdbName(simulatorName);
        log.info("[adb] - 模拟器: {}", adbConnectionName);

        return adbConnectionName;
    }


    /**
     * 查找模拟器
     */
    private String findAndBuildSimulator(Environment environment) {

        //查询现有的模拟器
        String simulatorName = findSimulatorFromDisk(environment);

        //是否新的模拟器
        boolean isNewSimulator = false;
        if (simulatorName == null) {
            isNewSimulator = true;
            simulatorName = findCustomName(Set.of(), environment.getName());
            log.info("[新建] - 模拟器: {}", simulatorName);
            //创建虚拟机
            LeiDianSimulatorUtil.create(leiDianHome, simulatorName);
            //修改模拟器
            LeiDianSimulatorUtil.modify(leiDianHome, simulatorName, (AndroidSimulatorDevice) environment.getDevice());
        } else {
            log.info("[寻到] - 模拟器: {}", simulatorName);
        }


        //重置虚拟机
        if (environment.isReset() && !isNewSimulator) {
            log.info("[重置] - 模拟器: {}", simulatorName);
            LeiDianSimulatorUtil.restore(leiDianHome, simulatorName);
        }

        return simulatorName;
    }


    /**
     * 构建服务端
     *
     * @return
     */
    private AppiumDriverLocalService buildService() {
        //服务端
        return appiumServer.buildService();
    }


    /**
     * 创建客户端
     */
    private AndroidDriver buildClient(AppiumDriverLocalService appiumDriverLocalService, String adbConnectionName) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("udid", adbConnectionName);
        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("newCommandTimeout", 60 * 60 * 24);

        //不允许脚本退出与关闭
        AndroidDriver driver = new AndroidDriver(appiumDriverLocalService.getUrl(), capabilities) {
            @Override
            public void quit() {
                log.info("不允许执行 : {}", "quit");
            }

            @Override
            public void close() {
                log.info("不允许执行 : {}", "close");
            }
        };
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return driver;
    }


    /**
     * 等待并取出adb的连接名
     *
     * @return
     */
    @SneakyThrows
    private String waitAndGetAdbName(String simulatorName) {
        String adbConnectionName = null;
        for (int i = 0; i < 5 * 60; i++) {
            adbConnectionName = LeiDianSimulatorUtil.getSerialno(leiDianHome, simulatorName);

            if (!StringUtils.hasText(adbConnectionName)) {
                Thread.sleep(3000);
                continue;
            }

            if ("unknown".equalsIgnoreCase(adbConnectionName)) {
                Thread.sleep(1000);
                continue;
            }

            if (adbConnectionName.indexOf("error:") > -1) {
                Thread.sleep(1000);
                continue;
            }

            return adbConnectionName;
        }
        return null;
    }


    /**
     * 通过内存找到模拟器
     *
     * @param environment
     * @return
     */
    private RunningSimulator findSimulatorFromCache(final Environment environment) {

        final List<Map<String, Object>> canUsedList = this.runningSimulators.stream()
                .filter((it) -> {
                    return !it.isWorking();
                }).filter((it) -> {
                    return Boolean.TRUE == LeiDianSimulatorUtil.isrunning(leiDianHome, it.getSimulatorName());
                }).map((it) -> {
                    return it.getInfo();
                }).collect(Collectors.toList());
        String name = findSimulatorFromList(environment, canUsedList);
        if (name == null) {
            return null;
        }
        RunningSimulator simulator = findRunningSimulatorByName(name);
        if (!LeiDianSimulatorUtil.isrunning(leiDianHome, simulator.getSimulatorName())) {
            return null;
        }
        simulator.setLastAccessTime(System.currentTimeMillis());
        simulator.setWorking(true);
        return simulator;
    }

    private RunningSimulator findRunningSimulatorByName(String name) {
        for (RunningSimulator simulator : this.runningSimulators) {
            if (simulator.getSimulatorName().equals(name)) {
                return simulator;
            }
        }
        return null;
    }


    /**
     * 查询现有的模拟器
     *
     * @return
     */
    private String findSimulatorFromList(final Environment environment, List<Map<String, Object>> canUsedList) {
        //模拟器配置的key
        final String playerName = "statusSettings.playerName";
        final String environmentName = environment.getName();

        final Set<String> canUsedNames = canUsedList.stream().map((it) -> {
            return String.valueOf(it.get(playerName));
        }).collect(Collectors.toSet());

        //通过名字匹配,若没有匹配到则直接返回
        if (StringUtils.hasText(environmentName)) {
            //目标查询的模拟器
            String simulatorName = findCustomName(canUsedNames, environmentName);
            if (canUsedNames.contains(simulatorName)) {
                return simulatorName;
            }
            return null;
        }

        //若没有要求的设备环境，则随意取一个
        if (environment.getDevice() == null && canUsedNames.size() > 0) {
            return canUsedNames.toArray(new String[0])[0];
        }

        //取出模拟器设备的要求
        final AndroidSimulatorDevice device = (AndroidSimulatorDevice) environment.getDevice();
        for (Map<String, Object> map : canUsedList) {
            if (matchSimulator(device, map)) {
                return String.valueOf(map.get(playerName));
            }
        }

        return null;
    }


    /**
     * 查询现有的模拟器
     *
     * @return
     */
    private String findSimulatorFromDisk(Environment environment) {

        //模拟器配置的key
        final String playerName = "statusSettings.playerName";

        final String userName = environment.getName();
        //获取现有的模拟器
        Map<String, Map<String, Object>> allList = LeiDianSimulatorUtil.list(leiDianHome);

        //取出正在运行的模拟器
        List<String> runningList = LeiDianSimulatorUtil.runninglist(leiDianHome);

        //取出可以用的模拟器(没有运行)
        final List<Map<String, Object>> canUsedList = allList.values().stream().filter((it) -> {
            Object pName = it.get(playerName);
            return pName != null && !runningList.contains(pName);
        }).collect(Collectors.toList());

        return this.findSimulatorFromList(environment, canUsedList);
    }

    /**
     * 模拟器是否匹配
     *
     * @return
     */
    @SneakyThrows
    private boolean matchSimulator(AndroidSimulatorDevice device, Map<String, Object> simulatorMap) {
        Map<String, Object> deviceMap = BeanUtil.toMap(device);
        if (!matchMap(deviceMap, "imei", simulatorMap, "propertySettings.phoneIMEI")) {
            return false;
        }

        if (!matchMap(deviceMap, "imsi", simulatorMap, "propertySettings.phoneIMSI")) {
            return false;
        }

        if (!matchMap(deviceMap, "simserial", simulatorMap, "propertySettings.phoneSimSerial")) {
            return false;
        }

        if (!matchMap(deviceMap, "androidid", simulatorMap, "propertySettings.phoneAndroidId")) {
            return false;
        }

        if (!matchMap(deviceMap, "model", simulatorMap, "propertySettings.phoneModel")) {
            return false;
        }

        if (!matchMap(deviceMap, "manufacturer", simulatorMap, "propertySettings.phoneManufacturer")) {
            return false;
        }

        if (!matchMap(deviceMap, "mac", simulatorMap, "propertySettings.macAddress")) {
            return false;
        }


        //分辨率
        if (!matchResolution(device.getResolution(), simulatorMap.get("advancedSettings.resolution"), simulatorMap.get("basicSettings.width"), simulatorMap.get("basicSettings.height"))) {
            return false;
        }
        return true;
    }


    /**
     * 判断分辨率是否匹配
     *
     * @param userResolution
     * @param simulatorWidth
     * @param simulatorHeight
     * @return
     */
    private boolean matchResolution(String userResolution, Object simulatorResolution, Object simulatorWidth, Object simulatorHeight) {
        if (userResolution == null) {
            return true;
        }
        if (simulatorWidth == null || simulatorHeight == null) {
            return false;
        }

        String[] userResolutions = userResolution.split(",");


        if (simulatorResolution != null) {
            Map<String, Object> ret = (Map<String, Object>) simulatorResolution;
            return userResolutions[0].equals(String.valueOf(ret.get("width"))) && userResolutions[1].equals(String.valueOf(ret.get("height")));
        }


        return userResolutions[0].equals(String.valueOf(simulatorWidth)) && userResolutions[1].equals(String.valueOf(simulatorHeight));
    }


    /**
     * 匹配配置
     *
     * @param deviceMap
     * @param dKey
     * @param simulatorMap
     * @param sKey
     */
    private boolean matchMap(Map<String, Object> deviceMap, String dKey, Map<String, Object> simulatorMap, String sKey) {
        Object dVal = deviceMap.get(dKey);
        if (dVal == null) {
            return true;
        }
        //取出值并转到字符串
        String dValues = String.valueOf(dVal);
        if (StringUtils.hasText(dValues) && !"auto".equalsIgnoreCase(dValues)) {
            return dValues.equals(simulatorMap.get(sKey));
        }
        return true;
    }


    /***
     * 构建新的自定义模拟器名称
     * @return
     */
    private String findCustomName(Set<String> canUsed, String userName) {
        //获取现有的模拟器
        Set<String> allNames = LeiDianSimulatorUtil.listNames(leiDianHome);

        //用户名
        if (userName != null) {

            //优先取出可以用的模拟器
            if (canUsed != null) {
                for (int i = 0; i < 50; i++) {
                    String simulatorName = buildSimulatorName(userName, i);
                    //如果在可用列表中找到则直接返回模拟器名
                    if (canUsed.contains(simulatorName)) {
                        return simulatorName;
                    }
                }
            }


            //如果找不到则返回新的模拟器名
            for (int i = 0; i < 50; i++) {
                String simulatorName = buildSimulatorName(userName, i);
                //如果在可用列表中找到则直接返回模拟器名
                if (!allNames.contains(simulatorName)) {
                    return simulatorName;
                }
            }
        }

        return String.format("%s_%s", CustomName, UUID.randomUUID().toString().replaceAll("-", ""));
    }

    /**
     * 构建模拟器名称
     *
     * @param userName
     * @param index
     * @return
     */
    private String buildSimulatorName(String userName, int index) {
        return String.format("%s_%s_%s", CustomName, userName, String.valueOf(index));
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RunningSimulator {
        //模拟器名称
        private String simulatorName;

        //取出adb的androidid
        private String adbConnectionName;

        //模拟器对应的本地服务
        private AppiumDriverLocalService appiumDriverLocalService;

        //是否正在工作
        private boolean working;

        //最后一次访问时间
        private long lastAccessTime;

        //android的驱动
        private AndroidDriver driver;

        //信息
        private Map<String, Object> info;

    }

}

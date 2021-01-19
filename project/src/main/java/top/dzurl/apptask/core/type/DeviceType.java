package top.dzurl.apptask.core.type;

import lombok.Getter;
import top.dzurl.apptask.core.runtime.RunTimeEnvironmentManager;
import top.dzurl.apptask.core.runtime.impl.AndroidMachineRunTimeManager;
import top.dzurl.apptask.core.runtime.impl.AndroidSimulatorRunTimeManager;

/**
 * 设备类型
 */
public enum DeviceType {
    //Android 模拟器
    AndroidSimulator(PlatformType.Android, AndroidSimulatorRunTimeManager.class),
    AndroidMachine(PlatformType.Android, AndroidMachineRunTimeManager.class),


    IosSimulator(PlatformType.Ios, null),
    IosMachine(PlatformType.Ios, null),


    ;

    DeviceType(PlatformType platform, Class<? extends RunTimeEnvironmentManager> runTimeManager) {
        this.platform = platform;
        this.runTimeManager = runTimeManager;
    }

    //平台
    @Getter
    private PlatformType platform;


    //环境构建工具
    @Getter
    private Class<? extends RunTimeEnvironmentManager> runTimeManager;


}


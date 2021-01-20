package top.dzurl.apptask.core.type;

import lombok.Getter;
import top.dzurl.apptask.core.model.ScriptRuntime;
import top.dzurl.apptask.core.model.runtime.AndroidMachineScriptRuntime;
import top.dzurl.apptask.core.model.runtime.AndroidSimulatorScriptRuntime;
import top.dzurl.apptask.core.runtime.RunTimeEnvironmentManager;
import top.dzurl.apptask.core.runtime.impl.AndroidMachineRunTimeManager;
import top.dzurl.apptask.core.runtime.impl.AndroidSimulatorRunTimeManager;

/**
 * 设备类型
 */
public enum DeviceType {
    //Android 模拟器
    AndroidSimulator(PlatformType.Android, AndroidSimulatorRunTimeManager.class, AndroidSimulatorScriptRuntime.class),
    AndroidMachine(PlatformType.Android, AndroidMachineRunTimeManager.class, AndroidMachineScriptRuntime.class),


    IosSimulator(PlatformType.Ios, null, null),
    IosMachine(PlatformType.Ios, null, null),


    ;

    DeviceType(PlatformType platform, Class<? extends RunTimeEnvironmentManager> runTimeManager, Class<? extends ScriptRuntime> scriptRuntime) {
        this.platform = platform;
        this.runTimeManager = runTimeManager;
        this.scriptRuntime = scriptRuntime;
    }

    //平台
    @Getter
    private PlatformType platform;


    //环境构建工具
    @Getter
    private Class<? extends RunTimeEnvironmentManager> runTimeManager;


    //脚本的运行环境
    @Getter
    private Class<? extends ScriptRuntime> scriptRuntime;


}


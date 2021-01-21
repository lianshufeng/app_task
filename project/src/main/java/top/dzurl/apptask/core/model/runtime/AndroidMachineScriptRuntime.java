package top.dzurl.apptask.core.model.runtime;

import lombok.Data;
import top.dzurl.apptask.core.model.ScriptRuntime;

/**
 * 脚本运行环境
 */
@Data
public class AndroidMachineScriptRuntime extends ScriptRuntime {

    //设备名
    private String deviceName;

}

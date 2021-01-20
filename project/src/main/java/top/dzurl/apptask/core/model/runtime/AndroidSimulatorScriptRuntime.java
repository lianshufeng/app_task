package top.dzurl.apptask.core.model.runtime;

import lombok.Data;
import top.dzurl.apptask.core.model.ScriptRuntime;

@Data
public class AndroidSimulatorScriptRuntime extends ScriptRuntime {
    //模拟器名称
    private String simulatorName;
}

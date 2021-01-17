package top.dzurl.apptask.core.runtime;

import top.dzurl.apptask.core.model.ScriptRuntime;

public interface RunTimeEnvironmentManager {

    /**
     * 创建环境
     *
     * @return
     */
    void open(ScriptRuntime runtime);


    /**
     * 关闭环境
     *
     */
    void close(ScriptRuntime runtime);


}

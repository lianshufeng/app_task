package top.dzurl.apptask.core.runtime.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.dzurl.apptask.core.model.ScriptRuntime;
import top.dzurl.apptask.core.runtime.RunTimeEnvironmentManager;

/**
 * android真机
 */
@Slf4j
@Component
public class AndroidMachineRunTimeManager implements RunTimeEnvironmentManager {

    @Override
    public void open(ScriptRuntime runtime) {

    }

    @Override
    public void close(ScriptRuntime runtime) {

    }
}

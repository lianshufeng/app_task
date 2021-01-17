package top.dzurl.apptask.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.dzurl.apptask.core.model.AppScript;
import top.dzurl.apptask.core.script.ScriptHelper;

@RestController
@RequestMapping({"script"})
public class ScriptController {

    @Autowired
    private ScriptHelper scriptHelper;


    /**
     * 执行脚本
     *
     * @param appScript
     * @return
     */
    @RequestMapping("execute")
    public Object execute(@RequestBody AppScript appScript) {
        return scriptHelper.executeScript(appScript.getScriptName(), appScript.getEnvironment(), appScript.getParameters());
    }


}

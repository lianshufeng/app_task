package top.dzurl.apptask.core.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.dzurl.apptask.core.model.AppScript;
import top.dzurl.apptask.core.script.ScriptHelper;
import top.dzurl.apptask.core.script.SuperScript;
import top.dzurl.apptask.core.script.UserInterface;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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


    /**
     * 获取正在执行的脚本
     *
     * @return
     */
    @RequestMapping("listRunScript")
    public Collection<ScriptModel> listRunScript() {
        return scriptHelper.getCurrentRunScript().entrySet().stream().map((entry) -> {
            return toScriptModel(entry.getKey(), entry.getValue());
        }).collect(Collectors.toList());
    }

    @RequestMapping("ui")
    public Object ui(UiModel uiModel) {
        SuperScript script = this.scriptHelper.getCurrentRunScript().get(uiModel.getScriptId());
        Assert.notNull(script, "脚本id不正确");
        UserInterface ui = script.getUi().remove(uiModel.getUiId());
        Assert.notNull(ui, "交互的ui的ID不正确");
        ui.proceed(uiModel.getCallbackParameter());
        return new HashMap<>() {
            {
                put("time", System.currentTimeMillis());
            }
        };
    }


    /**
     * 转换到脚本模型
     *
     * @param uuid
     * @param script
     * @return
     */
    private ScriptModel toScriptModel(String uuid, SuperScript script) {
        ScriptModel scriptModel = new ScriptModel();
        scriptModel.setId(uuid);
        scriptModel.setCreateTime(script.getCreateTime());

        //获取交互内容
        Map<String, UserInterface.Interact> interactMap = new HashMap<>();
        script.getUi().entrySet().forEach((entry) -> {
            interactMap.put(entry.getKey(), entry.getValue().getInteract());
        });
        scriptModel.setInteract(interactMap);

        scriptModel.setName(script.name());
        scriptModel.setRemark(script.remark());
        return scriptModel;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UiModel {
        //脚本id
        private String scriptId;

        //UI的ID
        private String uiId;

        //UI交互的参数
        private UserInterface.CallbackParameter callbackParameter;

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScriptModel {
        //脚本id
        private String id;

        //脚本开始时间
        private long createTime;

        //脚本名称
        private String name;

        //脚本脚本
        private String remark;

        //用户交互事件
        private Map<String, UserInterface.Interact> interact = new ConcurrentHashMap<>();

    }


}

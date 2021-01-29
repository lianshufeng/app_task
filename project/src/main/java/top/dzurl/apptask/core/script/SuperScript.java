package top.dzurl.apptask.core.script;

import groovy.lang.Script;
import lombok.Getter;
import top.dzurl.apptask.core.model.Environment;
import top.dzurl.apptask.core.model.Parameter;
import top.dzurl.apptask.core.model.ScriptRuntime;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本的父类
 */
public abstract class SuperScript extends Script {

    protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SuperScript.class);

    //脚本路径
    @Getter
    protected File scriptFile;

    @Getter
    protected ScriptRuntime runtime;

    //异步方法
    @Getter
    protected ScriptAsync async;

    //通用方法
    @Getter
    protected ScriptMethod method;

    //取出UI方法
    @Getter
    private Map<String, UserInterface> ui = new ConcurrentHashMap<>();

    //创建脚本的时间
    @Getter
    private long createTime = System.currentTimeMillis();


    /**
     * 添加用户交互
     */
    protected String addUI(UserInterface userInterface) {
        String uuid = UUID.randomUUID().toString();
        ui.put(uuid, userInterface);
        return uuid;
    }


    /**
     * 脚本名称
     *
     * @return
     */
    public abstract String name();


    /**
     * 环境
     *
     * @return
     */
    public Environment environment() {
        return new Environment();
    }


    /**
     * 参数
     *
     * @return
     */
    public Map<String, Parameter> parameters() {
        return new HashMap<>();
    }


    /**
     * 脚本事件
     *
     * @return
     */
    public ScriptEvent event() {
        return null;
    }


    /**
     * 脚本描述
     *
     * @return
     */
    public String remark() {
        return String.format("[%s] 脚本", this.name());
    }


    /**
     * 执行方法
     *
     * @return
     */
    public final Object execute() {
        return this.run();
    }


}

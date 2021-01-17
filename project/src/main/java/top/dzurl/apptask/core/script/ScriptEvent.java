package top.dzurl.apptask.core.script;

/**
 * 脚本事件
 */
public interface ScriptEvent {

    /**
     * 创建环境
     */
    void onCreate();

    /**
     * 运行应用
     */
    void onRunApp();

    /**
     * 关闭环境
     */
    void onClose();

}
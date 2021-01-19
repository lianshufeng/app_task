package top.dzurl.apptask.core.script;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

/**
 * 脚本事件
 */
public interface ScriptEvent {

    /**
     * 创建环境
     */
    void onCreate();


    /**
     * 在安装app
     */
    void onInstallApp();


    /**
     * 运行应用
     */
    void onRunApp();

    /**
     * 关闭环境
     */
    void onClose();


    /**
     * 事件对应的枚举类
     */
    public static enum EventType {
        Create(getMethod("onCreate")),
        InstallApp(getMethod("onInstallApp")),
        RunApp(getMethod("onRunApp")),
        Close(getMethod("onClose"));

        EventType(Method method) {
            this.method = method;
        }

        @Getter
        private Method method;


        @SneakyThrows
        private static Method getMethod(String methodName) {
            return ScriptEvent.class.getMethod(methodName, null);
        }

    }

}
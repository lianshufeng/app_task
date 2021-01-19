package top.dzurl.apptask.core.script;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

/**
 * 脚本事件
 */
public abstract class ScriptEvent {

    /**
     * 创建环境
     */
    public void onCreate() {
    }


    /**
     * 在安装app
     */
    public void onInstallApp() {
    }


    /**
     * 运行应用
     */
    public void onRunApp() {
    }


    /**
     * 关闭环境
     */
    public void onClose() {
    }


    /**
     * 异常
     *
     * @param e
     */
    public void onException(Exception e) {
    }

    /**
     * 事件对应的枚举类
     */
    public static enum EventType {
        Create(getMethod("onCreate", null)),
        InstallApp(getMethod("onInstallApp", null)),
        RunApp(getMethod("onRunApp", null)),
        Close(getMethod("onClose", null)),
        Exception(getMethod("onException", Exception.class)),


        ;

        EventType(Method method) {
            this.method = method;
        }

        @Getter
        private Method method;


        @SneakyThrows
        private static Method getMethod(String methodName, Class<?>... parameterTypes) {
            return ScriptEvent.class.getMethod(methodName, parameterTypes);
        }

    }

}
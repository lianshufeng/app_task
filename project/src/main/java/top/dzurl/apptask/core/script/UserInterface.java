package top.dzurl.apptask.core.script;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 用户交互
 */
@Slf4j

public class UserInterface {

    //阻塞线程
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    //交互的参数
    @Getter
    protected Interact interact;

    //脚本
    protected SuperScript script;

    //ui交互后的数据
    private CallbackParameter callbackParameter;


    /**
     * 构建用户交互助手
     *
     * @return
     */
    public static UserInterface build() {
        return new UserInterface();
    }


    @SneakyThrows
    public void then(Then then) {
        countDownLatch.await();
        runThen(then);
    }

    @SneakyThrows
    public void then(long time, Then then) {
        countDownLatch.await(time, TimeUnit.MILLISECONDS);
        runThen(then);
    }

    /**
     * 执行then方法
     *
     * @param then
     */
    private void runThen(Then then) {
        Optional.ofNullable(then).ifPresent((it) -> {
            then.run(this.callbackParameter);
        });
    }


    /**
     * 继续执行数据
     *
     * @param callbackParameter
     */
    public void proceed(CallbackParameter callbackParameter) {
        this.callbackParameter = callbackParameter;
        countDownLatch.countDown();
    }


    /**
     * 结果
     */
    @FunctionalInterface
    public interface Then<T extends CallbackParameter> {

        /**
         * 用户回调事件
         *
         * @param callback
         */
        void run(T callback);

    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Interact {

        //交互类型
        private UIType type;

        //提示文本
        private String message;

        //交互次数
        private int interactionCount;

        //参考数据
        private String[] buffer;

    }

    /**
     * 交互类型
     */
    public static enum UIType {
        Input(InputParameter.class, "用户输入"),
        Ocr(OcrParameter.class, "图文识别"),
        Tap(TapParameter.class, "点击图片"),
        Drag(DragParameter.class, "拖拽"),

        ;

        UIType(Class<? extends CallbackParameter> parameter, String remark) {
            this.parameter = parameter;
            this.remark = remark;
        }

        /**
         * 参数
         */
        @Getter
        private Class<? extends CallbackParameter> parameter;

        @Getter
        private String remark;

    }

    /**
     * 用户回调事件
     */
    public abstract class CallbackParameter {

    }


    /**
     * 输入
     */
    public class InputParameter extends CallbackParameter {

    }

    public class TapParameter extends CallbackParameter {

    }

    public class DragParameter extends CallbackParameter {

    }

    public class OcrParameter extends CallbackParameter {

    }

}

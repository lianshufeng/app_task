package top.dzurl.apptask.core.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Where<T> {

    //线程池
    private ScheduledExecutorService threadPool;

    //是否循环执行
    private boolean cycle;

    //是否停止执行
    private boolean stop;

    public Where(ScheduledExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public Where(ScheduledExecutorService threadPool, boolean cycle) {
        this.threadPool = threadPool;
        this.cycle = cycle;
    }


    /**
     * 执行代码
     *
     * @param condition
     * @param then
     */
    public Where<T> execute(long sleepTime, Condition<T> condition, Then<T> then) {
        Assert.state(!stop, "无法执行已终止的任务");
        threadPool.schedule(() -> {


            T ret = null;
            try {
                ret = condition.match();
            } catch (Exception e) {
                ret = null;
                e.printStackTrace();
                log.error(e.getMessage());
            }


            try {
                if (ret != null) {
                    then.run(ret);
                }
            } catch (Exception e) {
                ret = null;
                e.printStackTrace();
                log.error(e.getMessage());
            }


            //循环判断
            if (ret == null || (this.cycle && !this.stop)) {
                execute(sleepTime, condition, then);
            }
        }, sleepTime, TimeUnit.MILLISECONDS);

        return this;
    }


    /**
     * 执行
     *
     * @param sleepTime
     * @return
     */
    public Where<T> execute(long sleepTime, Runnable runnable) {
        Assert.state(!stop, "无法执行已终止的任务");
        threadPool.schedule(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
            if ((this.cycle && !this.stop)) {
                execute(sleepTime, runnable);
            }
        }, sleepTime, TimeUnit.MILLISECONDS);
        return this;
    }


    /**
     * 终止循环
     */
    public void cancel() {
        this.stop = true;
    }


    @FunctionalInterface
    public interface Condition<T> {

        /**
         * 非空则匹配
         *
         * @return
         */
        T match();

    }


    @FunctionalInterface
    public interface Then<T> {

        /**
         * 执行
         *
         * @param t
         */
        void run(T t);

    }


}

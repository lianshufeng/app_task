package top.dzurl.apptask.core.helper;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import top.dzurl.apptask.core.script.SuperScript;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ProxyMethodHelper {

    //缓存的返回结果
    private Map<String, Object> proxyRetCache = new ConcurrentHashMap<>();

    //调度器
    private ScheduledExecutorService executorService;


    @Getter
    @Setter
    private SuperScript script;


    @Autowired
    private void init(ApplicationContext applicationContext) {
        executorService = script.getRuntime().getThreadPool();
    }


    /**
     * 执行代理方法
     *
     * @param methodName
     * @param cacheTime
     * @param method
     * @param <T>
     * @return
     */
    @SneakyThrows
    public <T> T execute(String methodName, long cacheTime, ProxyMethod<T> method) {
        if (this.proxyRetCache.containsKey(methodName)) {
            log.info("hit cache : {} ", methodName);
            return (T) this.proxyRetCache.get(methodName);
        }
        //执行原本方法
        T ret = method.run();
        cacheResult(methodName, cacheTime, ret);
        return ret;
    }

    /**
     * 缓存结果
     *
     * @param methodName
     * @param cacheTime
     * @param ret
     * @param <T>
     */
    private <T> void cacheResult(String methodName, long cacheTime, T ret) {
        proxyRetCache.put(methodName, ret);
        executorService.schedule(() -> {
            proxyRetCache.remove(methodName);
        }, cacheTime, TimeUnit.MILLISECONDS);
    }


    /**
     * 代理方法
     *
     * @param <T>
     */
    @FunctionalInterface
    public interface ProxyMethod<T> {
        /**
         * 执行方法
         *
         * @return
         */
        T run();
    }


}

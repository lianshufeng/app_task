package top.dzurl.apptask.core.script;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import top.dzurl.apptask.core.conf.AppTaskConf;
import top.dzurl.apptask.core.helper.SpringBeanHelper;
import top.dzurl.apptask.core.model.Environment;
import top.dzurl.apptask.core.model.ScriptRuntime;
import top.dzurl.apptask.core.util.AppUtil;
import top.dzurl.apptask.core.util.ApplicationHomeUtil;
import top.dzurl.apptask.core.util.BeanUtil;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class ScriptHelper {


    public final static String PreScriptName = "script_";

    //脚本对象缓存
    private Map<String, String> scriptNameCache = new ConcurrentHashMap<>();

    //当前正在运行的脚本
    @Getter
    private Map<String, SuperScript> currentRunScript = new ConcurrentHashMap<>();


    //脚本存放的文件夹
    private final static File scriptFile = ApplicationHomeUtil.getResource("script");
    private static WatchService watchService;
    //定时器
    private Timer timer = new Timer();
    private Vector<Long> fileUpdateVector = new Vector();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpringBeanHelper springBeanHelper;

    @Autowired
    private AppTaskConf appTaskConf;

    //需要检查的文件
    private Vector<File> needCheckFile = new Vector();


    static {
        if (!scriptFile.exists()) {
            scriptFile.mkdirs();
        }
        log.info("script : {} ", scriptFile.getAbsolutePath());
    }


    /**
     * 初始化
     */
    @PostConstruct
    private void init() {
        //载入脚本
        loadAllScript();

        //监视
        watchFolder();

        //监视需要判断的脚本
        watchScriptUpdate();

    }


    /**
     * 监视目录
     */
    @SneakyThrows
    private void watchFolder() {
        watchService = FileSystems.getDefault().newWatchService();
        //注册监视目录
        Paths.get(scriptFile.getAbsolutePath()).register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW);
        new Thread(() -> {
            watchKey();
        }).start();
    }


    @SneakyThrows
    private void watchKey() {
        WatchKey key;
        while ((key = watchService.take()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                try {
                    //过滤groovy意外的脚本
                    String fileName = String.valueOf(event.context());
                    if (!"groovy".equals(FilenameUtils.getExtension(fileName))) {
                        continue;
                    }
//                    log.info("script update : {} -> {}", event.context(), event.kind());
                    //脚本路径
                    final File file = new File(scriptFile.getAbsolutePath() + "/" + event.context());
                    if (!needCheckFile.contains(file)) {
                        needCheckFile.add(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            key.reset();
        }
    }


    /**
     * 监视脚本发生变更
     */
    private void watchScriptUpdate() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (needCheckFile.size() > 0) {
                    File file = needCheckFile.remove(0);
                    if (file.exists()) {
                        removeScript(file);
                        updateScript(file);
                    } else {
                        removeScript(file);
                    }
                }
            }
        }, 100, 100);
    }


    /**
     * 更新脚本
     *
     * @param file
     */

    @SneakyThrows
    private synchronized void updateScript(File file) {
        //仅能加载groovy脚本
        if (!"groovy".equals(FilenameUtils.getExtension(file.getName()))) {
            return;
        }
        Script script = new GroovyShell().parse(file);
        if (!(script instanceof SuperScript)) {
            return;
        }
        SuperScript superScript = (SuperScript) script;
        //设置当前脚本的路径
        superScript.scriptFile = file;
        //取出脚本名
        String scriptName = superScript.name();

        //添加到到spring的容器中
        this.springBeanHelper.update(getComponentName(scriptName), superScript);

        //记录文件名与脚本名
        this.scriptNameCache.put(file.getName(), scriptName);

        log.info("load script : {} ", file.getName());
    }

    /**
     * 删除脚本
     *
     * @param file
     */
    private synchronized void removeScript(File file) {
        Optional.ofNullable(this.scriptNameCache.remove(file.getName())).ifPresent((scriptName) -> {
            //记录文件名与脚本名
            this.springBeanHelper.remove(getComponentName(scriptName));
            log.info("remove script : {} ", file.getName());
        });
    }


    /**
     * 载入脚本
     */
    private void loadAllScript() {
        Arrays.stream(scriptFile.listFiles()).filter((file) -> {
            return "groovy".equals(FilenameUtils.getExtension(file.getName()));
        }).forEach((file) -> {
            updateScript(file);
        });
    }


    /**
     * 执行脚本
     */
    @SneakyThrows
    public Object executeScript(final String scriptName, final Environment environment, final Map<String, Object> parameters) {
        //spring的组件名称
        final String componentName = getComponentName(scriptName);
        if (!this.applicationContext.containsBean(componentName)) {
            throw new RuntimeException(String.format("脚本 [%s] 不存在", scriptName));
        }
        Object o = this.applicationContext.getBean(componentName);
        if (o == null || !(o instanceof SuperScript)) {
            throw new RuntimeException(String.format("脚本 [%s] 不存在", scriptName));
        }

        //脚本执行的uuid
        final String uuid = UUID.randomUUID().toString();

        //实例化脚本对象
        SuperScript script = (SuperScript) new GroovyShell().parse(((SuperScript) o).getScriptFile());

        //当前正在运行的脚本
        this.currentRunScript.put(uuid, script);

        Object ret = null;
        try {
            ret = runScript(script, environment, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //移除正在执行的脚本
        this.currentRunScript.remove(uuid);

        return ret;
    }


    @SneakyThrows
    private Object runScript(SuperScript script, final Environment environment, final Map<String, Object> parameters) {

        //注入spring的对象
        this.springBeanHelper.injection(script);

        //设置脚本的代理方法
        this.setScriptProxyMethod(script, environment, parameters);

        //构建运行环境
        this.createRunTimeEnvironment(script);

        //打开app
        this.openAppOnRunTime(script);

        //执行脚本
        Object ret = null;
        try {
            ret = script.execute();
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            publishEvent(script, ScriptEvent.EventType.Exception);
        }

        //关闭运行环境
        this.closeRunTimeEnvironment(script);

        return ret;
    }


    /**
     * 构建运行环境
     */
    private void createRunTimeEnvironment(SuperScript script) {
        publishEvent(script, ScriptEvent.EventType.Create);

        //取出运行环境
        final ScriptRuntime runtime = script.getRuntime();
        final Environment environment = runtime.getEnvironment();


        //构建运行的环境（模拟器或者物理机）
        Optional.ofNullable(environment.getDevice().getType().getRunTimeManager()).ifPresent((runTimeManager) -> {
            this.applicationContext.getBeansOfType(runTimeManager).values().forEach((it) -> {
                it.open(runtime);
            });
        });


    }

    /**
     * 关闭运行环境
     */
    private void closeRunTimeEnvironment(SuperScript script) {
        publishEvent(script, ScriptEvent.EventType.Close);

        //取出运行环境
        final ScriptRuntime runtime = script.getRuntime();


        //关闭线程池
        Optional.ofNullable(runtime.getThreadPool()).ifPresent((it) -> {
            it.shutdownNow();
        });


        //关闭app
        Optional.ofNullable(getLaunchBundle(runtime)).ifPresent((it) -> {
            log.info("[app] - [close] - {}", it);
            script.getMethod().closeApp(it);
        });


        final Environment environment = runtime.getEnvironment();
        //构建运行的环境（模拟器或者物理机）
        Optional.ofNullable(environment.getDevice().getType().getRunTimeManager()).ifPresent((runTimeManager) -> {
            this.applicationContext.getBeansOfType(runTimeManager).values().forEach((it) -> {
                it.close(runtime);
            });
        });

    }

    /**
     * 在运行环境中安装并打开应用
     */
    private void openAppOnRunTime(SuperScript script) {

        //运行环境
        final ScriptRuntime runtime = script.getRuntime();

        //安装app
        Environment.App app = runtime.getEnvironment().getApp();
        if (app == null) {
            return;
        }

        // 没有驱动则不执行
        if (runtime.getDriver() == null) {
            return;
        }


        //事件：安装
        Optional.ofNullable(script.event()).ifPresent((it) -> {
            publishEvent(script, ScriptEvent.EventType.InstallApp);
        });

        //是否安装app,没有安装则安装
        Optional.ofNullable(app.getFileNames()).ifPresent((fileNames) -> {
            for (String fileName : fileNames) {
                File apkFile = appTaskConf.getRunTime().getApp().getAppHome(fileName);
                if (!apkFile.exists()) {
                    break;
                }
                //获取包名
                String bundleId = AppUtil.getBundleId(apkFile);
                if (!runtime.getDriver().isAppInstalled(bundleId)) {
                    log.info("[app] - [install] - {}", apkFile.getName());
                    runtime.getDriver().installApp(apkFile.getAbsolutePath());
                }
            }
        });


        //事件：app
        Optional.ofNullable(script.event()).ifPresent((it) -> {
            publishEvent(script, ScriptEvent.EventType.RunApp);
        });


        //启动路径
        Optional.ofNullable(getLaunchBundle(runtime)).ifPresent((bundleId) -> {
            log.info("[app] - [activate] - {}", bundleId);
            runtime.getDriver().activateApp(bundleId);
        });


    }

    /**
     * 发布事件
     */
    @SneakyThrows
    private void publishEvent(SuperScript script, ScriptEvent.EventType eventType, Object... args) {
        ScriptEvent scriptEvent = script.event();
        if (scriptEvent != null) {
            try {
                eventType.getMethod().invoke(scriptEvent, args);
            } catch (Exception e) {
                Optional.ofNullable(e.getMessage()).ifPresent((msg) -> {
                    log.error(msg);
                });
                try {
                    ScriptEvent.EventType.Exception.getMethod().invoke(scriptEvent, e);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


    /**
     * 获取需要启动的app
     *
     * @param runtime
     * @return
     */
    private String getLaunchBundle(ScriptRuntime runtime) {
        Environment.App app = runtime.getEnvironment().getApp();
        if (app == null) {
            return null;
        }
        Integer launchId = app.getLaunch();
        if (launchId == null) {
            return null;
        }
        return AppUtil.getBundleId(appTaskConf.getRunTime().getApp().getAppHome(app.getFileNames()[launchId]));
    }


    /**
     * 构建运行环境
     */
    @SneakyThrows
    private ScriptRuntime buildRunTime(Environment environment, Map<String, Object> parameters) {
        //构建脚本的运行环境
        ScriptRuntime scriptRuntime = environment.getDevice().getType().getScriptRuntime().getConstructor().newInstance();

        scriptRuntime.setEnvironment(environment);
        scriptRuntime.setParameters(parameters);
        //线程池
        scriptRuntime.setThreadPool(Executors.newScheduledThreadPool(environment.getThreadPoolCount()));
        return scriptRuntime;
    }

    /**
     * 设置脚本的代理方法
     */
    private void setScriptProxyMethod(final SuperScript script, final Environment userEnvironment, final Map<String, Object> userParameters) {
        //合并对象, 用户参数覆盖脚本预留参数
        final Environment runTimeEnvironment = mergeEnvironment(script.environment(), userEnvironment);

        //转换到脚本的参数,合并脚本参数
        final Map<String, Object> scriptParameters = new HashMap<>();
        script.parameters().entrySet().forEach((it) -> {
            scriptParameters.put(it.getKey(), it.getValue().getValue());
        });
        final Map<String, Object> runTimeParameters = mergeParameter(scriptParameters, userParameters);

        //构建运行脚本
        script.runtime = buildRunTime(runTimeEnvironment, runTimeParameters);

        //异步方法
        buildScriptAsync(script);

        //内置方法
        buildScriptMethod(script);

    }

    /**
     * 构建脚本的异步方法
     *
     * @param script
     */
    private void buildScriptAsync(final SuperScript script) {
        script.async = ScriptAsync.builder().script(script).build();
        springBeanHelper.injection(script.async);
    }


    /**
     * 构建脚本执行方法
     *
     * @param script
     */
    private void buildScriptMethod(final SuperScript script) {
        ScriptMethod.build(script);
        springBeanHelper.injection(script.method);
    }

    /**
     * 合并对象
     *
     * @return
     */
    private Environment mergeEnvironment(Environment target, Environment source) {
        Optional.ofNullable(source).ifPresent((it) -> {
            BeanUtils.copyProperties(it, target, new HashSet<>() {{
                addAll(BeanUtil.getNullPropertyNames(it));
                add("device");
            }}.toArray(new String[0]));

            //Device
            Optional.ofNullable(it.getDevice()).ifPresent((device) -> {
                //设备类型不存在则完全使用用户传递的设备信息
                if (target.getDevice() == null) {
                    target.setDevice(device);
                } else {
                    BeanUtils.copyProperties(device, target.getDevice(), new HashSet<>() {{
                        addAll(BeanUtil.getNullPropertyNames(device));
                    }}.toArray(new String[0]));
                }
            });
        });


        return target;
    }


    /**
     * 合并用户与脚本的参数
     *
     * @return
     */
    private Map<String, Object> mergeParameter(Map<String, Object>... sources) {
        Map<String, Object> ret = new HashMap<String, Object>();
        if (sources != null && sources.length > 0) {
            Arrays.stream(sources).forEach((source) -> {
                if (source != null) {
                    ret.putAll(source);
                }
            });
        }
        return ret;
    }


    /**
     * 转换到脚本名称
     *
     * @param name
     * @return
     */
    public static String getComponentName(String name) {
        return PreScriptName + name;
    }


}

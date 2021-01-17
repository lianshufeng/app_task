package top.dzurl.apptask.core.appium;

import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.dzurl.apptask.core.conf.AppTaskConf;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class AppiumServer {

    @Autowired
    private AppTaskConf appTaskConf;


    private File node_path;
    private File JS_Path;


    @PostConstruct
    private void init() {
        initPath();
    }

    //初始化路径
    private void initPath() {
        node_path = new File(this.appTaskConf.getRunTime().getNodeHome().getAbsolutePath() + "/node.exe");
        JS_Path = new File(this.appTaskConf.getRunTime().getAppiumHome().getAbsolutePath() + "/node_modules/appium/build/lib/main.js");
    }


    /**
     * 创建服务
     *
     * @return
     */
    public AppiumDriverLocalService buildService() {
        AppiumDriverLocalService service = AppiumDriverLocalService.buildService(new AppiumServiceBuilder()
                .withArgument(GeneralServerFlag.LOG_LEVEL, "error")
                .withArgument(GeneralServerFlag.SESSION_OVERRIDE)
                .withArgument(GeneralServerFlag.RELAXED_SECURITY)
                .usingAnyFreePort()
                .usingDriverExecutable(node_path)
                .withAppiumJS(JS_Path));
        service.start();
        return service;
    }


}

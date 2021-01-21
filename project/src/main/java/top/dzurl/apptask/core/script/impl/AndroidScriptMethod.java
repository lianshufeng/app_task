package top.dzurl.apptask.core.script.impl;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.html5.Location;
import org.springframework.beans.factory.annotation.Autowired;
import top.dzurl.apptask.core.conf.AppTaskConf;
import top.dzurl.apptask.core.helper.MapHelper;
import top.dzurl.apptask.core.model.ScriptRuntime;
import top.dzurl.apptask.core.model.runtime.AndroidSimulatorScriptRuntime;
import top.dzurl.apptask.core.script.ScriptMethod;
import top.dzurl.apptask.core.util.LeiDianSimulatorUtil;

import java.util.HashMap;

/**
 * android的方法
 */
@Slf4j
public class AndroidScriptMethod extends ScriptMethod {

    @Autowired
    private AppTaskConf appTaskConf;

    @Override
    public void closeApp(String bundleId) {
        script.getRuntime().getDriver().executeScript("mobile:shell", new HashMap<>() {{
            put("command", "am");
            put("args", new String[]{"force-stop", bundleId});
        }});
    }

    @Override
    public void openView(String url) {
        script.getRuntime().getDriver().executeScript("mobile:shell", new HashMap<>() {{
            put("command", "am");
            put("args", new String[]{
                    "start", "-a", "android.intent.action.VIEW", "-d", url
            });
        }});
    }

    @Override
    public boolean setLocation(String address) {
        MapHelper.Location location = this.mapHelper.query(address);
        log.info("{} -> {}", address, location);
        if (location == null) {
            return false;
        }
        return setLocation(location.getLng(), location.getLat());
    }

    @Override
    public boolean setLocation(String lng, String lat) {
        //运行环境
        final ScriptRuntime runtime = script.getRuntime();

        //如果是是模拟器则用模拟器内置方法进行定位
        if (runtime instanceof AndroidSimulatorScriptRuntime) {
            //取出模拟器名字
            final String simulatorName = ((AndroidSimulatorScriptRuntime) runtime).getSimulatorName();
            LeiDianSimulatorUtil.locate(appTaskConf.getRunTime().getSimulator().getHome(), simulatorName, lng, lat);
        } else {
            //真机用 Driver
            runtime.getDriver().setLocation(new Location(Double.valueOf(lat), Double.valueOf(lng), 0));
        }
        return true;
    }


}


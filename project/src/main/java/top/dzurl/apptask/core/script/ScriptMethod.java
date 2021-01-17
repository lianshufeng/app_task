package top.dzurl.apptask.core.script;

import org.openqa.selenium.html5.Location;
import org.springframework.beans.factory.annotation.Autowired;
import top.dzurl.apptask.core.helper.MapHelper;
import top.dzurl.apptask.core.type.PlatformType;

import java.util.HashMap;
import java.util.Optional;

public abstract class ScriptMethod {

    @Autowired
    protected MapHelper mapHelper;


    //脚本对象
    protected SuperScript script;


    /**
     * 关闭应用
     */
    public abstract void closeApp(String bundleId);

    /**
     * 打开视图
     *
     * @param url
     */
    public abstract void openView(String url);


    /**
     * 设置物理地址自动转坐标
     *
     * @param address
     */
    public abstract boolean setLocation(String address);


    /**
     * 设置经纬度
     *
     * @param lng
     * @param lat
     */
    public abstract boolean setLocation(String lng, String lat);


    /**
     * 构建脚本方法
     *
     * @param script
     * @return
     */
    protected static void build(SuperScript script) {

        //取出脚本运行的平台
        final PlatformType platformType = script.getRuntime().getEnvironment().getDevice().getType().getPlatform();

        ScriptMethod scriptMethod = null;
        if (platformType == PlatformType.Android) {
            scriptMethod = new AndroidScriptMethod();
        } else if (platformType == PlatformType.Ios) {
            //todo 未完成
            scriptMethod = null;
        }

        //初始化方法
        Optional.ofNullable(scriptMethod).ifPresent((it) -> {
            script.method = it;
            it.script = script;
        });

    }


    /**
     * android的方法
     */
    public static class AndroidScriptMethod extends ScriptMethod {


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
            if (location == null) {
                return false;
            }
            return setLocation(location.getLng(), location.getLat());
        }

        @Override
        public boolean setLocation(String lng, String lat) {
            script.getRuntime().getDriver().setLocation(new Location(Double.valueOf(lat), Double.valueOf(lng), 0));
            return true;
        }


    }


    /**
     * Ios的方法
     */
    public static abstract class IosScriptMethod extends ScriptMethod {

    }


}

package top.dzurl.apptask.core.script;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import top.dzurl.apptask.core.helper.MapHelper;
import top.dzurl.apptask.core.helper.ProxyMethodHelper;
import top.dzurl.apptask.core.helper.SpringBeanHelper;
import top.dzurl.apptask.core.runtime.model.Device;
import top.dzurl.apptask.core.script.impl.AndroidScriptMethod;
import top.dzurl.apptask.core.type.PlatformType;

import java.util.Optional;

@Slf4j
public abstract class ScriptMethod {

    @Autowired
    protected MapHelper mapHelper;


    //脚本对象
    protected SuperScript script;


    @Delegate(types = ProxyMethodHelper.class)
    private ProxyMethodHelper proxyMethodHelper;


    @Autowired
    private void initProxyMethodHelper(SpringBeanHelper springBeanHelper) {
        proxyMethodHelper = new ProxyMethodHelper();
        proxyMethodHelper.setScript(script);
        //注入spring对象
        springBeanHelper.injection(proxyMethodHelper);
    }


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
        //取出设备信息
        Device device = script.getRuntime().getEnvironment().getDevice();
        Assert.notNull(device, "设备类型不能为空");

        //取出脚本运行的平台
        final PlatformType platformType = device.getType().getPlatform();

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


}

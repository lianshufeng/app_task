package script

import com.google.common.collect.ImmutableMap
import io.appium.java_client.AppiumDriver
import io.appium.java_client.MobileDriver
import io.appium.java_client.MultiTouchAction
import io.appium.java_client.TouchAction
import io.appium.java_client.android.Activity
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidElement
import io.appium.java_client.android.nativekey.AndroidKey
import io.appium.java_client.android.nativekey.KeyEvent
import io.appium.java_client.ios.IOSDriver
import io.appium.java_client.touch.TapOptions
import io.appium.java_client.touch.offset.PointOption
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Point
import org.openqa.selenium.interactions.touch.TouchActions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import top.dzurl.apptask.core.model.Environment
import top.dzurl.apptask.core.model.Parameter
import top.dzurl.apptask.core.runtime.model.AndroidMachineDevice
import top.dzurl.apptask.core.runtime.model.AndroidSimulatorDevice
import top.dzurl.apptask.core.script.ScriptEvent
import top.dzurl.apptask.core.script.SuperScript
import top.dzurl.apptask.core.util.JsonUtil

import java.util.concurrent.TimeUnit

class DingDingDaKa extends SuperScript {
    @Autowired
    private ApplicationContext applicationContext;


    String appPackege = "com.alibaba.android.rimet"

    @Override
    Environment environment() {
        return [
                'app'   : [
                        'fileNames': ['dingding.apk']
                ],
                'device': [
                        'resolution': '1080,1920,280'
                ] as AndroidSimulatorDevice   //AndroidSimulatorDevice  AndroidMachineDevice
        ] as Environment
    }

    @Override
    Map<String, Parameter> parameters() {
        return [
                'phone'   : new Parameter(value: '13368172379', remark: '电话'),
                'password': new Parameter(value: 'xxx', remark: '密码'),
                'company' : new Parameter(value: 'xxx', remark: '公司名称')
        ]
    }

    @Override
    ScriptEvent event() {
        return [
                'onCreate'    : {
                    println 'onCreate'
                },
                'onInstallApp': {
                    println 'onInstallApp'
                },
                'onRunApp'    : {
                    //         denied,
                    //        granted,
                    //        requested;

                    AndroidDriver driver = getRuntime().getDriver()
                    ArrayList arrayList = getPermissions(driver, "granted")

                    method.setLocation('重庆解放碑')

                    String[] permissions = ["android.permission.MODIFY_AUDIO_SETTINGS", "android.permission.NFC", "android.permission.CHANGE_NETWORK_STATE", "android.permission.WRITE_SYNC_SETTINGS", "android.permission.RECEIVE_BOOT_COMPLETED", "android.permission.BLUETOOTH", "android.permission.CHANGE_WIFI_MULTICAST_STATE", "android.permission.GET_TASKS", "android.permission.AUTHENTICATE_ACCOUNTS", "android.permission.INTERNET", "android.permission.REORDER_TASKS", "android.permission.BLUETOOTH_ADMIN", "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS", "android.permission.BROADCAST_STICKY", "android.permission.CHANGE_WIFI_STATE", "android.permission.FLASHLIGHT", "android.permission.ACCESS_NETWORK_STATE", "android.permission.USE_FINGERPRINT", "android.permission.READ_SYNC_SETTINGS", "android.permission.VIBRATE", "android.permission.ACCESS_WIFI_STATE", "android.permission.WAKE_LOCK", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.READ_PHONE_STATE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.READ_PHONE_STATE", "android.permission.WRITE_EXTERNAL_STORAGE"]

                    for (String per:permissions){
                        if (!arrayList.contains(per)){

                            log.info('赋予权限：{}',per)
                            changePermissions(driver, per)
                        }
                    }
                    driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS)
                    driver.activateApp(appPackege)
                },
                'onClose'     : {

                    println 'onClose'
                },
                'onException' : {
                    it ->
                        ((Exception) it).printStackTrace()
                        async.proceed()

                }

        ] as ScriptEvent
    }

    @Override
    String name() {
        return "dddk"
    }

    @Override
    Object run() {
        AndroidDriver driver = getRuntime().getDriver();



        // 隐私界面
        privacyPolicyActivity(driver)

        //登陆流程
        newUserLoginActivity(driver)

        //重新登陆
        signUpWithPwdActivity(driver)

        //账户授权
        accountAuth(driver)

        // 打卡流程
        signIn(driver)


        async.await(10000*6*2)

        return [
                'text'       : 'ok123',
                'time'       : System.currentTimeMillis(),
                'environment': getRuntime().getEnvironment(),
                'thread'     : Thread.currentThread().getName(),
                'me'         : this.toString()
        ]
    }


    /**
     * 隐私页面
     * @param driver
     */
    void privacyPolicyActivity(AndroidDriver driver) {
        async.where(false).execute(300, {
            return targetPage(driver, ".PrivacyPolicyActivity")
        }, {
            log.info('进入隐私页面')

            driver.findElementByAndroidUIAutomator(findEelementId('com.alibaba.android.rimet:id/btn_agree')).click()
        })
    }


    /**
     * 登陆流程
     * @param driver
     */
    void newUserLoginActivity(AndroidDriver driver) {
        async.where(false).execute(300, {
            return targetPage(driver, "com.alibaba.android.user.login.NewUserLoginActivity")
        }, {
            log.info('进入登陆流程')


            driver.findElementByAndroidUIAutomator(findEelementId('com.alibaba.android.rimet:id/et_phone')).sendKeys(runtime.getParameters().get('phone'))
            driver.findElementByAndroidUIAutomator(findEelementId('com.alibaba.android.rimet:id/ll_container')).click()
            driver.findElementByAndroidUIAutomator(findEelementText('确认')).click()
            driver.findElementByAndroidUIAutomator(findEelementId('com.alibaba.android.rimet:id/et_pwd_login')).sendKeys(runtime.getParameters().get('password'))
            driver.findElementByAndroidUIAutomator(findEelementId('com.alibaba.android.rimet:id/tv')).click()


        })
    }

    /**
     * 重新登陆
     * @param driver
     */
    void signUpWithPwdActivity(AndroidDriver driver) {
        async.where(false).execute(300, {

            Boolean resl = targetPage(driver, "com.alibaba.android.user.login.SignUpWithPwdActivity") && driver.getPageSource().contains('更多选项')
            log.info('重新登陆：{}', resl)
            return resl == true ? true : null
        }, {
            log.info('进入登陆流程1')


            driver.findElementByAndroidUIAutomator(findEelementId('com.alibaba.android.rimet:id/et_phone_input')).sendKeys(runtime.getParameters().get('phone'))
            driver.findElementByAndroidUIAutomator(findEelementId('com.alibaba.android.rimet:id/et_pwd_login')).sendKeys(runtime.getParameters().get('password'))
            driver.findElementByAndroidUIAutomator(findEelementId('com.alibaba.android.rimet:id/tv')).click()


        })
    }


    /**
     * 用户授权
     * @param driver
     */
    void accountAuth(AndroidDriver driver) {
        async.where(false).execute(300, {
            return targetPage(driver, "com.alibaba.lightapp.runtime.activity.NoLoginCommonWebViewActivity")

        }, {
            log.info('用户验证登陆')
            driver.findElementByAndroidUIAutomator(findEelementText("继 续")).click()

            if (pageContains(driver,"你已绑定支付宝")){
                driver.findElementByAndroidUIAutomator(findEelementText("换个方式")).click()
            }

            driver.findElementByAndroidUIAutomator(findEelementText("能")).click()

            log.info('发送验证码')

            while (true){

                log.info("进入收短信流程")

                //todo 增加UI交互

                //接受验证码
                List<AndroidKey> keyList= new ArrayList<>();
                keyList.add(AndroidKey.NUMPAD_2)
                keyList.add(AndroidKey.NUMPAD_2)
                keyList.add(AndroidKey.NUMPAD_2)
                keyList.add(AndroidKey.NUMPAD_2)
                if (keyList.size()!=4){
                    log.info("这句话不被执行")

                    continue
                }

                for (AndroidKey key:keyList){
                    log.info("输入验证码")


                    driver.pressKey(new KeyEvent(key))
                }
                if (pageContains(driver,"验证码错误")){
                    driver.pressKey(new KeyEvent(AndroidKey.BACK))
//                    driver.findElementByAndroidUIAutomator(findEelementId('android:id/button1')).click()
                    continue
                }
                if (targetPage(driver, ".biz.LaunchHomeActivity") == true && driver.getPageSource().contains("通讯录")){
                    break
                }
                if(pageContains(driver,"重发验证码")){
                    driver.findElementByAndroidUIAutomator(findEelementText("重发验证码")).click()
                    continue
                }

                driver.pressKey(new KeyEvent(AndroidKey.BACK))



            }

        })
    }


    /**
     * 打卡页面
     * @param driver
     */
    void signIn(AndroidDriver driver) {
        async.where(false).execute(300, {
            if (targetPage(driver, ".biz.LaunchHomeActivity") == true) {
                String page = driver.getPageSource()

                Boolean result = page.contains('通讯录')
                log.info('进入打卡流程条件：{}', result)
                return result == true ? true : null;
            }

            return null

        }, {
            log.info('进入打卡流程')

            AndroidElement androidElement =driver.findElementByAndroidUIAutomator(findEelementId('com.alibaba.android.rimet:id/home_app_recycler_view'))

            Dimension dimension =androidElement.getSize()
            Point point =androidElement.getLocation()
            println driver.manage().window().getSize()


            int x = point.getX()+dimension.getWidth()/2
            int y = point.getY()+dimension.getHeight()/2
//
            log.info('坐标：{}，{}',x,y)
//
            TouchAction touchAction = new TouchAction(driver)
            touchAction.tap(PointOption.point(x,y)).perform()
            touchAction.tap(TapOptions.tapOptions().withTapsCount(2).withPosition(PointOption.point(x,y))).perform().release()

            changeCompany(driver, runtime.getParameters().get('company'))

            driver.findElementByAndroidUIAutomator(scrollaFindEelementText('考勤打卡')).click()
//
            if (pageContains(driver,'开启全新考勤')) {
                driver.findElementByAndroidUIAutomator(findEelementText('开启全新考勤')).click()
            }

            if (pageContains(driver,'外勤打卡')){
                driver.findElementByAndroidUIAutomator(findEelementText('外勤打卡')).click()
            }

            if (pageContains(driver,'考勤打卡')){
                driver.findElementByAndroidUIAutomator(findEelementText('考勤打卡')).click()
            }


            async.proceed()
        })

    }


    /**
     * 页面包含目标内容
     * @param driver
     * @param content
     * @return
     */
    boolean pageContains(AndroidDriver driver,String content){
        for (int i=0;i<5;i++){

            if (driver.getPageSource().contains(content)){
                return true
            }
            Thread.sleep(500)
        }
        return false

    }


    /**
     * 更改公司
     * @param driver
     */
    void changeCompany(AndroidDriver driver, String company) {
        AndroidElement element = driver.findElement(By.id('com.alibaba.android.rimet:id/menu_current_company'))
        if (!element.getText().contains(company)) {
            element.click()
            driver.findElementByAndroidUIAutomator(findEelementText(company)).click()
        }
    }


    /**
     * 滚动查找
     * @param text
     * @return
     */

    String scrollaFindEelementText(String text) {
        return String.format("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector().text(\"%s\"))", text);
    }


    /**
     * autoUi resourceId
     * @param id
     * @return
     */
    String findEelementId(String id) {

        return String.format('new UiSelector().resourceId("%s")', id)

    }


    /**
     *  autoUi resourceId
     * @param text
     * @return
     */
    String findEelementText(String text) {

        return String.format('new UiSelector().text("%s")', text)
    }

    /**
     * 目标页面
     * @param driver
     * @param pack
     * @param activity
     * @return
     */
    Boolean targetPage(AndroidDriver driver, String activity) {

        def ret=method.execute('targetPage',1000,{
            return  [driver.getCurrentPackage(),driver.currentActivity()]
        })
        String targetPack = ret[0]
        String targetActivity = ret[1]
        log.info("当前包名:{}", targetPack)
        log.info("当前活动名:{}", targetActivity)
        if (appPackege.equals(targetPack) && activity.equals(targetActivity)) {
            return true
        }
        return null
    }


    /**
     * 获取APP权限列表
     * @param driver
     * @param appPackage
     * @param type
     * @return
     */
    ArrayList getPermissions(AndroidDriver driver, String type) {
        return (ArrayList) ((JavascriptExecutor) driver).executeScript("mobile:getPermissions", ImmutableMap.of(
                "appPackage", appPackege, "type", type));
    }

    /**
     * 更改权限
     * @param driver
     * @param appPackage
     * @param permission
     * @param authority
     */
    void changePermissions(AndroidDriver driver, String... authority) {
        ((JavascriptExecutor) driver).executeScript("mobile:changePermissions", ImmutableMap.of(
                "appPackage", appPackege, "action", "grant", "permissions", authority));
    }

}

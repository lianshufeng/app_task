package script

import io.appium.java_client.android.AndroidDriver
import top.dzurl.apptask.core.model.Environment
import top.dzurl.apptask.core.runtime.model.AndroidMachineDevice
import top.dzurl.apptask.core.script.ScriptEvent
import top.dzurl.apptask.core.script.SuperScript
import top.dzurl.apptask.core.util.JsonUtil

class DingDingDaKa extends SuperScript {


    @Override
    ScriptEvent event() {
        return [
                'onCreate': {
                    println 'onCreate'
                },
                'onRunApp': {
                    println 'onRunApp'
                },
                'onClose' : {
                    println "onClose"
                }
        ] as ScriptEvent
    }

    @Override
    String name() {
        return "dddk"
    }


    @Override
    Environment environment() {
        return [
                'app'   : [
                        'fileNames': ['dingding.apk']
                ],
                'device': [
                ] as AndroidMachineDevice
        ] as Environment
    }


    @Override
    Object run() {

        AndroidDriver driver = runtime.getDriver();
        println JsonUtil.toJson(driver.getBatteryInfo(), true)

        return [
                'text'       : 'ok123',
                'time'       : System.currentTimeMillis(),
                'environment': getRuntime().getEnvironment(),
                'thread'     : Thread.currentThread().getName(),
                'me'         : this.toString()
        ]
    }


}

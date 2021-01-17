package script


import io.appium.java_client.android.AndroidDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import top.dzurl.apptask.core.model.Environment
import top.dzurl.apptask.core.model.Parameter
import top.dzurl.apptask.core.runtime.model.AndroidSimulatorDevice
import top.dzurl.apptask.core.script.ScriptEvent
import top.dzurl.apptask.core.script.SuperScript

//仅测试的时候需要
class test extends SuperScript {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    Environment environment() {
        return [
                'app'   : [
                        'fileNames': ['test.apk'],
                        'launch'   : 0
                ],
                'device': [
                        'resolution': '1080,1920,280'
                ] as AndroidSimulatorDevice
        ] as Environment
    }

    @Override
    Map<String, Parameter> parameters() {
        return [
                'time': new Parameter(value: 1000, remark: '定时器')
        ]
    }

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
                    println 'onClose'
                }

        ] as ScriptEvent
    }

    @Override
    String name() {
        return "TestScript1"
    }

    @Override
    Object run() {
        AndroidDriver driver = getRuntime().getDriver();


//        println driver.executeScript("mobile: shell", ['command': 'ps']);

//        println driver.executeScript("mobile:shell", [
//                'command': 'am',
//                'args'   : 'start -a android.intent.action.VIEW -d http://www.qq.com'.split(' ')
//        ])


//        method.openView()

        async.await(1000)

        return [
                'text'       : 'ok123',
                'time'       : System.currentTimeMillis(),
                'environment': getRuntime().getEnvironment(),
                'thread'     : Thread.currentThread().getName(),
                'me'         : this.toString()
        ]
    }


}
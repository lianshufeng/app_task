package script


import top.dzurl.apptask.core.model.Environment
import top.dzurl.apptask.core.runtime.model.AndroidMachineDevice
import top.dzurl.apptask.core.script.ScriptEvent
import top.dzurl.apptask.core.script.SuperScript

class DingDingDaKa extends SuperScript {


    @Override
    ScriptEvent event() {
        return [
                'onCreate': {
                    println 'onCreate'
                },
                'onRunApp': {
                    println 'onRunApp'
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

        return null
    }


}

package top.dzurl.apptask.core.runtime.model;

import lombok.Data;
import top.dzurl.apptask.core.type.DeviceType;

/**
 * 非模拟器仅支持部分修改
 */
@Data
public class AndroidMachineDevice extends Device {


    @Override
    public DeviceType getType() {
        return DeviceType.AndroidMachine;
    }
}

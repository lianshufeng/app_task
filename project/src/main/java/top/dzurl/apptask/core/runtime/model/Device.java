package top.dzurl.apptask.core.runtime.model;

import com.fasterxml.jackson.annotation.*;
import top.dzurl.apptask.core.type.DeviceType;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备
 */


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
//@type=AndroidSimulatorDevice
@JsonSubTypes({
        @JsonSubTypes.Type(value = AndroidSimulatorDevice.class, name = "AndroidSimulatorDevice"),
        @JsonSubTypes.Type(value = AndroidMachineDevice.class, name = "AndroidMachineDevice")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Device {

    //设备类型
    @JsonIgnore
    public abstract DeviceType getType();


    private Map<String, Object> _properties = new HashMap<String, Object>();

    @JsonAnySetter
    public Map<String, Object> any() {
        return _properties;
    }

    @JsonAnySetter
    public void set(String key, Object value) {
        _properties.put(key, value);
    }


}

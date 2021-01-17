package top.dzurl.apptask.core.runtime.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.dzurl.apptask.core.type.DeviceType;

/**
 * 非模拟器仅支持部分修改
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AndroidSimulatorDevice extends Device {

    //分辨率 <w,h,dpi>
    private String resolution;

    //Cpu的核心 <1 | 2 | 3 | 4>
    private Integer cpu;

    //内存 <256 | 512 | 768 | 1024 | 1536 | 2048 | 4096 | 8192>
    private Integer memory;

    //手机品牌  asus
    private String manufacturer;

    // 手机型号 ASUS_Z00DUO
    private String model;

    //手机号码
    private String pnumber;

    // imei <auto | 865166023949731>
    private String imei;

    // imei <auto | 460000000000000>
    private String imsi;

    // 序列号 <auto | 89860000000000000000>
    private String simserial;

    // android id <auto | 0123456789abcdef>
    private String androidid;

    // mac地址 <auto | 000000000000>
    private String mac;

    //自动翻转 <1 | 0>
    private Integer autorotate;

    // 锁定窗口 <1 | 0>
    private Integer lockwindow;


    @Override
    public DeviceType getType() {
        return DeviceType.AndroidSimulator;
    }
}

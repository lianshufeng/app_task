package top.dzurl.apptask.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * android的基本信息
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AndroidDeviceInfo {

    //系统版本
    private String version;

    //系统api版本
    private String sdk;

    //手机设备型号
    private String productModel;

    //手机厂商名称
    private String productBrand;

    //手机序列号
    private String serialno;

    //mac地址
    private String mac;

}

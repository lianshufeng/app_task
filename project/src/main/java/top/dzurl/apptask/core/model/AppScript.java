package top.dzurl.apptask.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 应用脚本
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppScript {

    //应用名
    private String scriptName;

    //环境参数
    private Environment environment;

    //脚本参数
    private Map<String, Object> parameters;

}

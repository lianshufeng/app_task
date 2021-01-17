package top.dzurl.apptask.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonMerge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.dzurl.apptask.core.runtime.model.Device;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Environment {

    //设备
    private Device device;

    //是否需要全新的环境(仅模拟器有效，会重置模拟器)
    private boolean reset;

    //环境名称
    private String name;

    //应用
    private App app;

    //线程池数量
    private int threadPoolCount = 10;


    //应用环境
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class App {

        //应用名
        private String[] fileNames;

        //启动
        private Integer launch;

    }


}

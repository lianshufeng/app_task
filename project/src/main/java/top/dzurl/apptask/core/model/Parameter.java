package top.dzurl.apptask.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 参数
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Parameter {


    //参数描述
    private String remark;

    //参数默认值
    private Object value;


}

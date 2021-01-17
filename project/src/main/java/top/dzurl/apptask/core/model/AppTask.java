package top.dzurl.apptask.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 应用的任务描述
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppTask extends AppScript {

    //调度器表达式
    private String cron;

}

package top.dzurl.apptask.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.concurrent.ScheduledFuture;


@Data
public class TaskModel extends AppTask {

    //任务id
    private String id;


    @JsonIgnore
    private ScheduledFuture scheduledFuture;

}

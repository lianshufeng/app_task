package top.dzurl.apptask.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.dzurl.apptask.core.model.TaskModel;
import top.dzurl.apptask.core.service.TaskService;

@RestController
@RequestMapping({"task"})
public class TaskController {

    @Autowired
    private TaskService taskService;


    /**
     * 删除任务
     *
     * @param taskId
     * @return
     */
    @RequestMapping("remove")
    public Object remove(String taskId) {
        return taskService.remove(taskId);
    }

    /**
     * 新增/更新任务
     *
     * @param taskModel
     * @return
     */
    @RequestMapping("update")
    public Object update(@RequestBody TaskModel taskModel) {
        return this.taskService.update(taskModel);
    }


    @RequestMapping("list")
    public Object list() {
        return this.taskService.list();
    }


}

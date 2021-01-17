package top.dzurl.apptask.core.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import top.dzurl.apptask.core.conf.AppTaskConf;
import top.dzurl.apptask.core.model.TaskModel;
import top.dzurl.apptask.core.result.ResultContent;
import top.dzurl.apptask.core.script.ScriptHelper;
import top.dzurl.apptask.core.util.ApplicationHomeUtil;
import top.dzurl.apptask.core.util.JsonUtil;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Service
@EnableScheduling
public class TaskService {

    /**
     * 动态调度器
     */
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AppTaskConf appTaskConf;

    //任务列表
    private Map<String, TaskModel> tasks = new ConcurrentHashMap<>();

    //保存的资源路径
    private File file = ApplicationHomeUtil.getResource("tasks.json");

    //任务执行的线程池
    protected ExecutorService executorService;

    @Autowired
    private void initThreadPool(ApplicationContext applicationContext) {
        executorService = Executors.newFixedThreadPool(this.appTaskConf.getMaxRunTaskCount());
    }


    /**
     * 查询所有任务
     *
     * @return
     */
    public ResultContent<Collection<TaskModel>> list() {
        return ResultContent.buildContent(tasks.values());
    }


    /**
     * 删除任务
     *
     * @param taskId
     * @return
     */
    public ResultContent<Object> remove(String taskId) {
        Optional.ofNullable(tasks.remove(taskId)).ifPresent((it) -> {
            it.getScheduledFuture().cancel(true);
        });
        save();
        return ResultContent.buildContent(taskId);
    }


    /**
     * 更新任务
     *
     * @param taskModel
     * @return
     */
    public TaskModel update(TaskModel taskModel) {
        Assert.hasText(taskModel.getScriptName(), "脚本名不能为空");
        Assert.hasText(taskModel.getCron(), "调度器表达式不能为空");

        if (StringUtils.hasText(taskModel.getId())) {
            this.remove(taskModel.getId());
        } else {
            taskModel.setId(UUID.randomUUID().toString());
        }


        //表达式
        taskModel.setScheduledFuture(threadPoolTaskScheduler.schedule(applicationContext.getBean(TaskRunnable.class).setTaskModel(taskModel), new CronTrigger(taskModel.getCron())));
        tasks.put(taskModel.getId(), taskModel);
        save();
        return taskModel;
    }


    @Autowired
    @SneakyThrows
    private void load(ApplicationContext applicationContext) {
        if (!file.exists()) {
            return;
        }
        String ret = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
        List<Map<String, Object>> sources = JsonUtil.toObject(ret, List.class);

        for (Map<String, Object> it : sources) {
            log.info("load task : {}", it);
            this.update(JsonUtil.toObject(JsonUtil.toJson(it), TaskModel.class));
        }
    }

    @SneakyThrows
    private void save() {
        FileUtils.writeByteArrayToFile(file, JsonUtil.toJson(tasks.values(), true).getBytes(StandardCharsets.UTF_8));
    }


    @Data
    @Slf4j
    @Component
    @Scope("prototype")
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    private static class TaskRunnable implements Runnable {

        @Autowired
        private TaskService taskService;

        @Autowired
        private ScriptHelper scriptHelper;

        //任务模型
        private TaskModel taskModel;


        @Override
        public void run() {
            log.info("execute {} -> {} ", taskModel.getId(), taskModel.getScriptName());
            taskService.executorService.execute(() -> {
                scriptHelper.executeScript(taskModel.getScriptName(), taskModel.getEnvironment(), taskModel.getParameters());
            });
        }
    }

}

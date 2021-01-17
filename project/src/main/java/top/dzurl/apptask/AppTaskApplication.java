package top.dzurl.apptask;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import top.dzurl.apptask.core.util.JsonUtil;

@Slf4j
@SpringBootApplication
@ComponentScan("top.dzurl.apptask.core")
public class AppTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppTaskApplication.class, args);

        //打印环境变量
        printEnv();
    }

    private static void printEnv() {
        log.info("env : {} ", JsonUtil.toJson(System.getenv()));
    }

}

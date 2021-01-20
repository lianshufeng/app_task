package top.dzurl.apptask.core.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.task")
public class AppTaskConf {

    //最大并发任务的数量
    private int maxRunTaskCount = 5;

    //运行环境的配置
    private RunTime runTime;


    /**
     * 模拟器
     */

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunTime {

        //运行环境的跟目录
        protected File home;

        @Getter
        public Simulator simulator = new Simulator(this);


        @Getter
        public App app = new App(this);

        /**
         * 获取Appium的路径
         *
         * @return
         */
        public File getAppiumHome() {
            return new File(FilenameUtils.normalize(home.getAbsolutePath() + "/appium"));
        }

        /**
         * 获取nodejs的路径
         *
         * @return
         */
        public File getNodeHome() {
            return new File(FilenameUtils.normalize(home.getAbsolutePath() + "/node"));
        }


        /**
         * 取出adb所在的路径
         *
         * @return
         */
        public File getADBHome() {
            return new File(FilenameUtils.normalize(home.getAbsolutePath() + "/android-sdk/platform-tools"));
        }


    }

    @NoArgsConstructor
    public static class App {

        private RunTime runTime;

        public App(RunTime runTime) {
            this.runTime = runTime;
        }


        /**
         * 获取模拟器
         *
         * @return
         */
        public File getHome() {
            return new File(runTime.getHome().getAbsolutePath() + "/../apps");
        }


        /**
         * 获取应用
         *
         * @param fileName
         * @return
         */
        public File getAppHome(String fileName) {
            return new File(getHome().getAbsolutePath() + "/" + fileName);
        }

    }

    /**
     * 模拟器
     */
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Simulator {

        private RunTime runTime;

        public Simulator(RunTime runTime) {
            this.runTime = runTime;
        }

        @Getter
        private int maxCacheCount = 3;

        /**
         * 获取模拟器
         *
         * @return
         */
        public File getHome() {
            return new File(runTime.getHome().getAbsolutePath() + "/simulator");
        }

    }

}

package top.dzurl.apptask.core.util;

import org.springframework.boot.system.ApplicationHome;

import java.io.File;

public class ApplicationHomeUtil {


    /**
     * 获取home的路径
     *
     * @return
     */
    public static File getHomeFile() {
        return new ApplicationHome(ApplicationHomeUtil.class).getDir();
    }


    /**
     * 获取资源路径
     *
     * @param fileName
     * @return
     */
    public static File getResource(String fileName) {
        return new File(getHomeFile().getAbsolutePath() + "/" + fileName);
    }


}

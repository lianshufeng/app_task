package top.dzurl.apptask.core.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import top.dzurl.apptask.core.util.android.AndroidXMLPrinter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class AppUtil {


    /**
     * 获取应用包名
     *
     * @param file
     * @return
     */
    public static String getBundleId(File file) {
        if (!file.exists()) {
            return null;
        }
        if ("apk".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
            return getAndroidBundleId(file);
        }
        return null;
    }

    /**
     * 获取android的包名
     *
     * @return
     */
    @SneakyThrows
    private static String getAndroidBundleId(File file) {
        @Cleanup FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipUtil.readFileStream(fileInputStream, "AndroidManifest.xml", outputStream);
        //转到输入流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return AndroidXMLPrinter.getInfo(byteArrayInputStream).getPackageName();
    }


}

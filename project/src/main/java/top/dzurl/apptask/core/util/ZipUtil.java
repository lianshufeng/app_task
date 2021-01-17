package top.dzurl.apptask.core.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * ZIP 压缩工具
 *
 * @作者: 练书锋
 * @联系: 251708339@qq.com
 */
public class ZipUtil {

    // 默认压缩文件的编码
    private static final String DefaultCharset = "GBK";

    /**
     * 压缩文件
     *
     * @param m
     * @throws IOException
     */
    public static void zip(final OutputStream outputStream, final Map<String, File> m) {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, Charset.forName(DefaultCharset));
        for (Entry<String, File> entry : m.entrySet()) {
            try {
                if (entry.getValue().exists()) {
                    // 数据
                    ZipEntry zipEntry = new ZipEntry(entry.getKey());
                    zipOutputStream.putNextEntry(zipEntry);
                    FileInputStream fileInputStream = new FileInputStream((entry.getValue()));
                    try {
                        StreamUtils.copy(fileInputStream, zipOutputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        fileInputStream.close();
                        zipOutputStream.closeEntry();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            zipOutputStream.finish();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    /**
     * 压缩资源文件 注：压缩管道结束后，必须执行 finish
     *
     * @throws IOException
     */
    public static void zip(final ZipOutputStream zipOutputStream, final String fileName, final InputStream inputStream)
            throws IOException {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);
        StreamUtils.copy(inputStream, zipOutputStream);
    }

    /**
     * 压缩目录
     *
     * @param outputStream
     * @param directory
     */
    public static void zipDirectory(final OutputStream outputStream, final File directory) {
        if (directory.exists()) {
            Map<String, File> m = new HashMap<String, File>();
            listFile(directory.getAbsolutePath(), directory, m);
            zip(outputStream, m);
        }
    }

    /**
     * 解压文件
     *
     * @throws IOException
     */
    public static List<String> unZipFile(final File sourceZip, final File targetDirectory) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(sourceZip);
        List<String> list = unZipFile(fileInputStream, targetDirectory);
        fileInputStream.close();
        return list;
    }


    /**
     * 读取zip流
     *
     * @param fileName
     * @param outputStream
     */
    @SneakyThrows
    public static void readFileStream(final InputStream inputStream, String fileName, final OutputStream outputStream) {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            try {
                if (fileName.equals(zipEntry.getName())) {
                    StreamUtils.copy(zipInputStream, outputStream);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        zipInputStream.closeEntry();
    }


    public static List<String> unZipFile(final InputStream inputStream, final File targetDirectory) throws IOException {
        List<String> list = new ArrayList<String>();
        // 创建或清空目录
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            try {
                final File file = new File(targetDirectory.getAbsolutePath() + "/" + zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File pFile = new File(file.getParent());
                    if (!pFile.exists()) {
                        pFile.mkdirs();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    StreamUtils.copy(zipInputStream, fileOutputStream);
                    fileOutputStream.close();
                    list.add(zipEntry.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            zipEntry.clone();
            zipInputStream.closeEntry();
        }

        return list;
    }

    /**
     * 深度遍历取出文件路径
     */
    private static void listFile(final String rootDirectory, final File directory, final Map<String, File> m) {
        if (directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                if (f.isDirectory()) {
                    listFile(rootDirectory, f, m);
                } else {
                    String fileName = relativeName(rootDirectory, f.getAbsolutePath());
                    m.put(fileName, f);
                }
            }
        } else {
            String fileName = relativeName(rootDirectory, directory.getAbsolutePath());
            m.put(fileName, directory);
        }
    }

    /**
     * 取出相对路径
     *
     * @param rootDirectory
     * @param directory
     * @return
     */
    public static String relativeName(final String rootDirectory, final String directory) {
        // 取出相对路径
        String path = relative(rootDirectory, directory);
        // 保证第一个出现的字符不能为/
        if (path.length() > 0) {
            if (path.substring(0, 1).equals("/")) {
                path = path.substring(1, path.length());
            }
        }
        return path;
    }


    /**
     * 取出相当路径
     *
     * @param rootPath 根目录
     * @param path
     * @return
     */
    private static String relative(final String rootPath, final String path) {
        String formatRootPath = format(rootPath);
        String formatPath = format(path);
        String left = formatPath.substring(0, formatRootPath.length());
        if (left.equals(formatRootPath)) {
            return formatPath.substring(formatRootPath.length(), formatPath.length());
        }
        return null;
    }

    /**
     * 格式化文件
     *
     * @param file
     * @return
     */
    private static String format(final String file) {
        String path = file;
        while (path.indexOf("\\") > -1) {
            path = path.replaceAll("\\\\", "/");
        }
        while (path.indexOf("//") > -1) {
            path = path.replaceAll("//", "/");
        }
        return path;
    }


}
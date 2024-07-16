package com.dygj.main.safe.out;

import com.dygj.main.utils.HttpUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 目标：通过伪造IP访问对外共享资源并下载，达到oss服务器流量消耗
 * 地址：http://z.hottermusic.com/resources/4EZbrPeaZ6dM8cMaJCzXxCZQZybaANZY.mp4   【189MB】 * 1 = 27.272秒
 *      http://z.hottermusic.com/resources/nxnj5kW4Qwh5BmaJs2ZaszHwSSKcDy87.mp3 【2.8MB】 * 10 = 4.188秒
 *
 * @Auther:Lipf
 */
public class FlushIO {

    public static void main(String[] args) {
        long begin = System.currentTimeMillis();
        System.out.println("开始时间："+begin+"\n");

        int num = 0;
        String url = "http://z.hottermusic.com/resources/4EZbrPeaZ6dM8cMaJCzXxCZQZybaANZY.mp4",
                saveDir = "C:\\Users\\Administrator\\Desktop\\dowloadFiles";
        File file = new File(saveDir);
        while (num++ < 100) {
            String fileName = num + url.substring(url.lastIndexOf("."));
            downloadByNIO(url, saveDir, fileName);
            System.out.println(fileName);

            // 满10个文件清理一次
            if (num % 10 == 0) {
                // 删除文件
                deleteDirectoryFiles(file);
                System.out.println("---------文件删除成功---------\n");
            }
        }
        System.out.println("下载结束！");

        long end = System.currentTimeMillis();
        System.out.println("\n结束时间："+end);
        System.out.println("共用时："+((end-begin)/1000.0)+"秒");
    }


    /**
     * 删除文件夹及其所有子文件夹和文件
     *
     * @param dir
     */
    public static void deleteDirectoryFiles(File dir) {
        if (dir.isDirectory()) {
            File[] entries = dir.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectoryFiles(entry);
                }
            }
        }
        dir.delete();
    }

    /**
     * 使用NIO下载文件
     *
     * @param url      下载地址
     * @param saveDir  保存地址
     * @param fileName 文件名称
     */
    public static void downloadByNIO(String url, String saveDir, String fileName) {
        try (InputStream ins = HttpUtils.getUrlInputStream(null, url, "GET", true)) {
            Path target = Paths.get(saveDir, fileName);
            Files.createDirectories(target.getParent());
            Files.copy(ins, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("downloadByNIO error from remoteUrl", e);
        }
    }


}

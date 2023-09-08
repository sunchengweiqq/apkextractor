package com.github.ghmxr.apkextractor.utils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.InputStream;
import java.util.zip.CRC32;

public class FileUtil {

    /**
     * 获取文件，文件夹的大小，单位字节
     *
     * @return 文件或文件夹大小，单位字节
     */
    public static long getFileOrFolderSize(File file) {
        try {
            if (file == null) return 0;
            if (!file.exists()) return 0;
            if (file.isFile()) return file.length();
            if (file.isDirectory()) {
                long total = 0;
                File[] files = file.listFiles();
                if (files == null || files.length == 0) return 0;
                for (File f : files) {
                    total += getFileOrFolderSize(f);
                }
                return total;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }



    public static CRC32 getCRC32FromInputStream(@NonNull InputStream inputStream) throws Exception {
        CRC32 crc = new CRC32();
        byte[] bytes = new byte[1024];
        int length;
        while ((length = inputStream.read(bytes)) != -1) {
            crc.update(bytes, 0, length);
        }
        inputStream.close();
        return crc;
    }
}

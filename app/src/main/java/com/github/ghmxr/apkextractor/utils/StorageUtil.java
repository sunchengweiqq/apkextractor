package com.github.ghmxr.apkextractor.utils;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.github.ghmxr.apkextractor.MyApplication;

import java.io.File;

public class StorageUtil {
    /**
     * 获取指定path的可写入存储容量，单位字节
     */
    public static long getAvaliableSizeOfPath(@NonNull String path) {
        try {
            StatFs stat = new StatFs(path);
            int version = Build.VERSION.SDK_INT;
            long blockSize = version >= 18 ? stat.getBlockSizeLong() : stat.getBlockSize();
            long availableBlocks = version >= 18 ? stat.getAvailableBlocksLong() : stat.getAvailableBlocks();
            return blockSize * availableBlocks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取外部存储主路径
     */
    @NonNull
    public static String getMainExternalStoragePath() {
        try {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getAppExternalStoragePath() {
        File[] externalFilesDirs = MyApplication.getApplication().getExternalFilesDirs(null);
        if (externalFilesDirs != null && externalFilesDirs.length > 0) {
            return externalFilesDirs[0].getAbsolutePath();
        }
        return "";
    }

}

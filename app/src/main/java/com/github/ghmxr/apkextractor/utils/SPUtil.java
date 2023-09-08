package com.github.ghmxr.apkextractor.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.github.ghmxr.apkextractor.Constants;

public class SPUtil {


    /**
     * 获取全局配置
     */
    public static SharedPreferences getGlobalSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 判断是否存储到了外置设备上
     *
     * @return true-存储到了外置存储上
     */
    public static boolean getIsSaved2ExternalStorage(@NonNull Context context) {
        return getGlobalSharedPreferences(context).getBoolean(Constants.PREFERENCE_STORAGE_PATH_EXTERNAL, Constants.PREFERENCE_STORAGE_PATH_EXTERNAL_DEFAULT);
    }



}

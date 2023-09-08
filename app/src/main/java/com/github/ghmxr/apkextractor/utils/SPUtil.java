package com.github.ghmxr.apkextractor.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.github.ghmxr.apkextractor.Constants;

public class SPUtil {

    public static SharedPreferences getGlobalSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public static boolean getIsSaved2ExternalStorage(@NonNull Context context) {
        return getGlobalSharedPreferences(context).getBoolean(Constants.PREFERENCE_STORAGE_PATH_EXTERNAL, Constants.PREFERENCE_STORAGE_PATH_EXTERNAL_DEFAULT);
    }



}

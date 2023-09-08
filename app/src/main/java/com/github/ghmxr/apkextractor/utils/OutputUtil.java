package com.github.ghmxr.apkextractor.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.github.ghmxr.apkextractor.Constants;
import com.github.ghmxr.apkextractor.items.AppItem;

import java.io.OutputStream;
import java.util.Calendar;

public class OutputUtil {

    /**
     * 为AppItem获取一个内置存储绝对写入路径
     *
     * @param extension "apk"或者"zip"
     */
    public static @NonNull
    String getAbsoluteWritePath(@NonNull Context context, @NonNull AppItem item, @NonNull String extension, int sequence_number) {
        SharedPreferences settings = SPUtil.getGlobalSharedPreferences(context);
        return settings.getString(Constants.PREFERENCE_SAVE_PATH, Constants.PREFERENCE_SAVE_PATH_DEFAULT)
                + "/" + getWriteFileNameForAppItem(context, item, extension, sequence_number);
    }






    /**
     * 为一个AppItem获取一个写入的文件名，例如example.apk
     */
    public static @NonNull
    String getWriteFileNameForAppItem(@NonNull Context context, @NonNull AppItem item, @NonNull String extension, int sqNum) {
        SharedPreferences settings = SPUtil.getGlobalSharedPreferences(context);
        if (extension.equalsIgnoreCase("apk")) {
            String result = String.valueOf(settings.getString(Constants.PREFERENCE_FILENAME_FONT_APK, Constants.PREFERENCE_FILENAME_FONT_DEFAULT));
            result = result.replace(Constants.FONT_APP_NAME, EnvironmentUtil.removeIllegalFileNameCharacters(String.valueOf(item.getAppName())));
            result = result.replace(Constants.FONT_APP_PACKAGE_NAME, String.valueOf(item.getPackageName()));
            result = result.replace(Constants.FONT_APP_VERSIONCODE, String.valueOf(item.getVersionCode()));
            result = result.replace(Constants.FONT_APP_VERSIONNAME, String.valueOf(item.getVersionName()));
            result = result.replace(Constants.FONT_YEAR, EnvironmentUtil.getCurrentTimeValue(Calendar.YEAR));
            result = result.replace(Constants.FONT_MONTH, EnvironmentUtil.getCurrentTimeValue(Calendar.MONTH));
            result = result.replace(Constants.FONT_DAY_OF_MONTH, EnvironmentUtil.getCurrentTimeValue(Calendar.DAY_OF_MONTH));
            result = result.replace(Constants.FONT_HOUR_OF_DAY, EnvironmentUtil.getCurrentTimeValue(Calendar.HOUR_OF_DAY));
            result = result.replace(Constants.FONT_MINUTE, EnvironmentUtil.getCurrentTimeValue(Calendar.MINUTE));
            result = result.replace(Constants.FONT_SECOND, EnvironmentUtil.getCurrentTimeValue(Calendar.SECOND));
            if (result.contains(Constants.FONT_AUTO_SEQUENCE_NUMBER)) {
                result = result.replace(Constants.FONT_AUTO_SEQUENCE_NUMBER, String.valueOf(sqNum));
            }
            result = result + ".apk";
            return result;
        } else {
            String result = String.valueOf(settings.getString(Constants.PREFERENCE_FILENAME_FONT_ZIP, Constants.PREFERENCE_FILENAME_FONT_DEFAULT));
            result = result.replace(Constants.FONT_APP_NAME, EnvironmentUtil.removeIllegalFileNameCharacters(String.valueOf(item.getAppName())));
            result = result.replace(Constants.FONT_APP_PACKAGE_NAME, String.valueOf(item.getPackageName()));
            result = result.replace(Constants.FONT_APP_VERSIONCODE, String.valueOf(item.getVersionCode()));
            result = result.replace(Constants.FONT_APP_VERSIONNAME, String.valueOf(item.getVersionName()));
            result = result.replace(Constants.FONT_YEAR, EnvironmentUtil.getCurrentTimeValue(Calendar.YEAR));
            result = result.replace(Constants.FONT_MONTH, EnvironmentUtil.getCurrentTimeValue(Calendar.MONTH));
            result = result.replace(Constants.FONT_DAY_OF_MONTH, EnvironmentUtil.getCurrentTimeValue(Calendar.DAY_OF_MONTH));
            result = result.replace(Constants.FONT_HOUR_OF_DAY, EnvironmentUtil.getCurrentTimeValue(Calendar.HOUR_OF_DAY));
            result = result.replace(Constants.FONT_MINUTE, EnvironmentUtil.getCurrentTimeValue(Calendar.MINUTE));
            result = result.replace(Constants.FONT_SECOND, EnvironmentUtil.getCurrentTimeValue(Calendar.SECOND));
            if (result.contains(Constants.FONT_AUTO_SEQUENCE_NUMBER)) {
                result = result.replace(Constants.FONT_AUTO_SEQUENCE_NUMBER, String.valueOf(sqNum));
            }
            result = result + "." + extension;
            return result;
        }

    }
}

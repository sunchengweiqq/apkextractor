package com.github.ghmxr.apkextractor;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.PermissionChecker;

import com.github.ghmxr.apkextractor.utils.SPUtil;
import com.github.ghmxr.apkextractor.utils.StorageUtil;

public class Constants {

    /**
     * this preference stands for a string value;
     */
    public static final String PREFERENCE_NAME = "settings";
    /**
     * this preference stands for a string value;
     */
    public static final String PREFERENCE_SAVE_PATH = "savepath";

    public static final String PREFERENCE_SAVE_PATH_DEFAULT;

    static {
        final Context context = MyApplication.getApplication();
        //takePersistUriPermission方法是从api19开始的
        if (Build.VERSION.SDK_INT < 19 || PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            PREFERENCE_SAVE_PATH_DEFAULT = StorageUtil.getMainExternalStoragePath() + "/Backup";
        } else {
            PREFERENCE_SAVE_PATH_DEFAULT = StorageUtil.getAppExternalStoragePath();
        }
        SharedPreferences sp = SPUtil.getGlobalSharedPreferences(context);
        if (!sp.contains(PREFERENCE_SAVE_PATH)) {
            sp.edit().putString(PREFERENCE_SAVE_PATH, PREFERENCE_SAVE_PATH_DEFAULT).apply();
        }
    }

    /**
     * this preference stands for a boolean value;
     */
    public static final String PREFERENCE_STORAGE_PATH_EXTERNAL = "save_external";
    public static final boolean PREFERENCE_STORAGE_PATH_EXTERNAL_DEFAULT = false;

    /**
     * this preference stands for a int value
     */
    public static final String PREFERENCE_MAIN_PAGE_VIEW_MODE = "main_view_mode";
    public static final int PREFERENCE_MAIN_PAGE_VIEW_MODE_DEFAULT = 1;
    /**
     * this preference stands for a boolean value;
     */
    public static final String PREFERENCE_SHOW_SYSTEM_APP = "show_system_app";

    public static final boolean PREFERENCE_SHOW_SYSTEM_APP_DEFAULT = false;

    /**
     * stands for a boolean value
     */
    public static final String PREFERENCE_LOAD_PERMISSIONS = "load_permissions";
    /**
     * stands for a boolean value
     */
    public static final String PREFERENCE_LOAD_ACTIVITIES = "load_activities";
    /**
     * stands for a boolean value
     */
    public static final String PREFERENCE_LOAD_RECEIVERS = "load_receivers";
    /**
     * boolean value
     */
    public static final String PREFERENCE_LOAD_SERVICES = "load_services";
    /**
     * boolean value
     */
    public static final String PREFERENCE_LOAD_PROVIDERS = "load_providers";
    /**
     * stands for a boolean value
     */
    public static final String PREFERENCE_LOAD_STATIC_LOADERS = "load_static_receivers";
    /**
     * stands for a boolean value
     */
    public static final String PREFERENCE_LOAD_APK_SIGNATURE = "load_apk_signature";
    /**
     * stands for a boolean value
     */
    public static final String PREFERENCE_LOAD_FILE_HASH = "load_file_hash";

    public static final String PREFERENCE_LOAD_NATIVE_FILE = "load_native_file";



    public static final boolean PREFERENCE_LOAD_PERMISSIONS_DEFAULT = true;
    public static final boolean PREFERENCE_LOAD_ACTIVITIES_DEFAULT = true;
    public static final boolean PREFERENCE_LOAD_RECEIVERS_DEFAULT = true;
    public static final boolean PREFERENCE_LOAD_SERVICES_DEFAULT = true;
    public static final boolean PREFERENCE_LOAD_PROVIDERS_DEFAULT = true;
    public static final boolean PREFERENCE_LOAD_STATIC_LOADERS_DEFAULT = true;
    public static final boolean PREFERENCE_LOAD_APK_SIGNATURE_DEFAULT = true;
    public static final boolean PREFERENCE_LOAD_FILE_HASH_DEFAULT = true;
    public static final boolean PREFERENCE_LOAD_NATIVE_FILE_DEFAULT = true;



    public static final String ACTION_REFRESH_APP_LIST = "com.github.ghmxr.apkextractor.refresh_applist";
    public static final String ACTION_REFRESH_IMPORT_ITEMS_LIST = "com.github.ghmxr.apkextractor.refresh_import_items_list";
    public static final String ACTION_REFRESH_AVAILIBLE_STORAGE = "com.github.ghmxr.apkextractor.refresh_storage";

}

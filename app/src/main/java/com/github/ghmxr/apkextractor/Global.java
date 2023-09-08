package com.github.ghmxr.apkextractor;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.ghmxr.apkextractor.items.AppItem;

import java.util.List;
import java.util.Vector;

public class Global {

    /**
     * 全局Handler，用于向主UI线程发送消息
     */
    public static final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 用于持有对读取出的list的引用
     */
    public static final List<AppItem> app_list = new Vector<>();


    /**
     * 从API30开始，外置data和obb路径不能直接使用File访问，使用documentFile访问data和obb的版本
     */
    public static final int USE_DOCUMENT_FILE_SDK_VERSION = Build.VERSION_CODES.R;

    /**
     * 从API33开始，对data和obb使用单独的文件夹进行访问
     */
    public static final int USE_STANDALONE_DOCUMENT_FILE_PERMISSION = 33;


    /**
     * 通过包名获取指定list中的item
     *
     * @param list         要遍历的list
     * @param package_name 要定位的包名
     * @return 查询到的AppItem
     */
    @Deprecated
    public static @Nullable
    AppItem getAppItemByPackageNameFromList(@NonNull List<AppItem> list, @NonNull String package_name) {
        for (AppItem item : list) {
            try {
                if (item.getPackageName().trim().toLowerCase().equals(package_name.trim().toLowerCase()))
                    return item;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 使用主线程执行操作
     */
    public static void runOnUiThread(@NonNull Runnable action) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            action.run();
        } else {
            handler.post(action);
        }
    }

}

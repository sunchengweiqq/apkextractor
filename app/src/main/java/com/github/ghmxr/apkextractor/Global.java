package com.github.ghmxr.apkextractor;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.ghmxr.apkextractor.items.AppItem;
import com.github.ghmxr.apkextractor.items.ImportItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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

    private static class ImportItemVector extends Vector<ImportItem> {

        @Override
        public synchronized boolean add(ImportItem importItem) {
            if (contains(importItem)) {
                return false;
            }
            return super.add(importItem);
        }

        /**
         * 重写此集合实现类addAll方法来去除重复添加的同path的元素
         */
        @Override
        public synchronized boolean addAll(@NonNull Collection<? extends ImportItem> c) {
            HashSet<ImportItem> hashSet = new HashSet<>(c);
            Iterator<ImportItem> iterator = hashSet.iterator();
            ImportItem importItem;
            while (iterator.hasNext()) {
                importItem = iterator.next();
                if (contains(importItem)) {
                    iterator.remove();
                }
            }
            return super.addAll(hashSet);
        }
    }

    public static final String URI_DATA = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata";
    public static final String URI_OBB = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb";

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

package com.github.ghmxr.apkextractor;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.github.ghmxr.apkextractor.items.AppItem;
import com.github.ghmxr.apkextractor.items.FileItem;
import com.github.ghmxr.apkextractor.items.ImportItem;
import com.github.ghmxr.apkextractor.utils.DocumentFileUtil;
import com.github.ghmxr.apkextractor.utils.OutputUtil;
import com.github.ghmxr.apkextractor.utils.SPUtil;

import java.io.File;
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

    /**
     * 导出目录下的文件list引用
     */
    public static final List<ImportItem> item_list = new ImportItemVector();

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

    /**
     * 通过FileItem的path从指定list中取出ImportItem
     *
     * @param list 要遍历的list
     * @param path FileItem的path，参考{@link FileItem#getPath()}
     * @return 指定的ImportItem
     */
    public static @Nullable
    ImportItem getImportItemByFileItemPath(@NonNull List<ImportItem> list, @NonNull String path) {
        for (ImportItem importItem : list) {
            try {
                if (importItem.getFileItem().getPath().equalsIgnoreCase(path)) return importItem;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 清除导出列表对应的zipinfo缓存
     *
     * @param path 导出列表对应item的path
     */
    public static void clearZipInfoCacheOfImportItemByPath(String path) {
        ImportItem importItem = getImportItemByFileItemPath(Global.item_list, path);
        if (importItem != null) {
            importItem.setZipFileInfo(null);
        }
    }

    private static String getDuplicatedFileInfo(@NonNull Context context, @NonNull List<AppItem> items) {
        if (items.size() == 0) return "";
        StringBuilder builder = new StringBuilder();
        boolean external = SPUtil.getIsSaved2ExternalStorage(context);
        if (external) {
            for (int i = 0; i < items.size(); i++) {
                final AppItem item = items.get(i);
                try {
                    DocumentFile searchFile = DocumentFileUtil.findDocumentFile(OutputUtil.getExportPathDocumentFile(context), OutputUtil.getWriteFileNameForAppItem(context, item, (item.exportData || item.exportObb) ?
                            SPUtil.getCompressingExtensionName(context) : "apk", i));
                    if (searchFile != null) {
                        builder.append(DocumentFileUtil.getDisplayPathForDocumentFile(context, searchFile));
                        builder.append("\n\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            for (int i = 0; i < items.size(); i++) {
                final AppItem item = items.get(i);
                File file = new File(OutputUtil.getAbsoluteWritePath(context, item, (item.exportData || item.exportObb) ? SPUtil.getCompressingExtensionName(context) : "apk", i + 1));
                if (file.exists()) {
                    builder.append(file.getAbsolutePath());
                    builder.append("\n\n");
                }
            }
        }

        return builder.toString();
    }


}

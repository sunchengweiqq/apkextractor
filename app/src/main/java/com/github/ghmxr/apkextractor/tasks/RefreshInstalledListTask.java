package com.github.ghmxr.apkextractor.tasks;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

import com.github.ghmxr.apkextractor.Constants;
import com.github.ghmxr.apkextractor.Global;
import com.github.ghmxr.apkextractor.MyApplication;
import com.github.ghmxr.apkextractor.items.AppItem;
import com.github.ghmxr.apkextractor.utils.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 刷新已安装的应用列表
 */
public class RefreshInstalledListTask extends Thread {
    private final Context context;
    private final boolean flag_system;
    private final RefreshInstalledListTaskCallback listener;
    private final List<AppItem> list_sum = new ArrayList<>();
    private volatile boolean isInterrupted = false;

    public RefreshInstalledListTask(@Nullable RefreshInstalledListTaskCallback callback) {
        this.context = MyApplication.getApplication();
        this.flag_system = SPUtil.getGlobalSharedPreferences(context).getBoolean(Constants.PREFERENCE_SHOW_SYSTEM_APP, Constants.PREFERENCE_SHOW_SYSTEM_APP_DEFAULT);
        this.listener = callback;
    }

    @Override
    public void run() {
        PackageManager manager = context.getApplicationContext().getPackageManager();
        final List<PackageInfo> list;
        synchronized (RefreshInstalledListTask.class) {
            list = new ArrayList<>(manager.getInstalledPackages(0));
        }
        Global.handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) listener.onRefreshProgressStarted(list.size());
            }
        });
        for (int i = 0; i < list.size(); i++) {
            if (isInterrupted) {
                return;
            }
            PackageInfo info = list.get(i);
            boolean info_is_system_app = ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
            final int current = i + 1;
            Global.handler.post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) listener.onRefreshProgressUpdated(current, list.size());
                }
            });
            if (!flag_system && info_is_system_app) continue;
            list_sum.add(new AppItem(context, info));
        }
        if (isInterrupted) {
            return;
        }

        Global.app_list.clear();
        Global.app_list.addAll(list_sum);
        GetSignatureInfoTask.clearCache();
        GetApkLibraryTask.clearOutsidePackageCache();

        GetPackageInfoViewTask.clearPackageInfoCache();
        HashTask.clearResultCache();
        Global.handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) listener.onRefreshCompleted(list_sum);
            }
        });

    }

    public void setInterrupted() {
        this.isInterrupted = true;
    }

    public interface RefreshInstalledListTaskCallback {
        void onRefreshProgressStarted(int total);

        void onRefreshProgressUpdated(int current, int total);

        void onRefreshCompleted(List<AppItem> appList);
    }
}

package com.github.ghmxr.apkextractor.tasks;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.ghmxr.apkextractor.Constants;
import com.github.ghmxr.apkextractor.Global;
import com.github.ghmxr.apkextractor.R;
import com.github.ghmxr.apkextractor.ui.AssemblyView;
import com.github.ghmxr.apkextractor.utils.EnvironmentUtil;
import com.github.ghmxr.apkextractor.utils.SPUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GetPackageInfoViewTask extends Thread {
    private final Activity activity;
    private final PackageInfo packageInfo;
    private final CompletedCallback callback;
    private final AssemblyView assemblyView;

    private static final ConcurrentHashMap<String, PackageInfo> cache_wrapped_package_info = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Bundle> cache_static_receivers = new ConcurrentHashMap<>();

    public GetPackageInfoViewTask(@NonNull Activity activity, @NonNull PackageInfo packageInfo
            , @NonNull AssemblyView assemblyView
            , @NonNull CompletedCallback callback) {
        this.activity = activity;
        this.packageInfo = packageInfo;
        this.assemblyView = assemblyView;
        this.callback = callback;
    }

    public static class AssembleItem<T extends ComponentInfo> {
        public T item;
        public View.OnClickListener clickAction;
        public View.OnLongClickListener longClickAction;

    }

    public static class StaticLoaderItem {
        public String name;
        public View.OnClickListener clickAction;
        public final ArrayList<IntentFilterItem> intentFilterItems = new ArrayList<>();

        public static class IntentFilterItem {
            public View intentFilterView;
        }
    }

    @Override
    public void run() {
        super.run();
        final SharedPreferences settings = SPUtil.getGlobalSharedPreferences(activity);
        String[] permissions = null;
        ActivityInfo[] activities = null;
        ActivityInfo[] receivers = null;
        ServiceInfo[] services = null;
        ProviderInfo[] providers = null;

        final boolean get_permissions = settings.getBoolean(Constants.PREFERENCE_LOAD_PERMISSIONS, Constants.PREFERENCE_LOAD_PERMISSIONS_DEFAULT);
        final boolean get_activities = settings.getBoolean(Constants.PREFERENCE_LOAD_ACTIVITIES, Constants.PREFERENCE_LOAD_ACTIVITIES_DEFAULT);
        final boolean get_receivers = settings.getBoolean(Constants.PREFERENCE_LOAD_RECEIVERS, Constants.PREFERENCE_LOAD_RECEIVERS_DEFAULT);
        final boolean get_static_loaders = settings.getBoolean(Constants.PREFERENCE_LOAD_STATIC_LOADERS, Constants.PREFERENCE_LOAD_STATIC_LOADERS_DEFAULT);
        final boolean get_services = settings.getBoolean(Constants.PREFERENCE_LOAD_SERVICES, Constants.PREFERENCE_LOAD_SERVICES_DEFAULT);
        final boolean get_providers = settings.getBoolean(Constants.PREFERENCE_LOAD_PROVIDERS, Constants.PREFERENCE_LOAD_PROVIDERS_DEFAULT);

        try {
            int flag = 0;
            if (get_permissions) flag |= PackageManager.GET_PERMISSIONS;
            if (get_activities) flag |= PackageManager.GET_ACTIVITIES;
            if (get_receivers) flag |= PackageManager.GET_RECEIVERS;
            if (get_services) flag |= PackageManager.GET_SERVICES;
            if (get_providers) flag |= PackageManager.GET_PROVIDERS;

            PackageInfo wrapped_package_info = cache_wrapped_package_info.get(packageInfo.applicationInfo.sourceDir);
            if (wrapped_package_info == null) {
                wrapped_package_info = activity.getPackageManager().getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, flag);
                cache_wrapped_package_info.put(packageInfo.applicationInfo.sourceDir, wrapped_package_info);
            }

            permissions = wrapped_package_info.requestedPermissions;
            activities = wrapped_package_info.activities;
            receivers = wrapped_package_info.receivers;
            services = wrapped_package_info.services;
            providers = wrapped_package_info.providers;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bundle static_receiver_bundle = cache_static_receivers.get(packageInfo.applicationInfo.sourceDir);
        if (static_receiver_bundle == null) {
            static_receiver_bundle = EnvironmentUtil.getStaticRegisteredReceiversOfBundleTypeForPackageName(activity, packageInfo.packageName);
            cache_static_receivers.put(packageInfo.applicationInfo.sourceDir, static_receiver_bundle);
        }


        final ArrayList<View> permission_child_views = new ArrayList<>();
        final ArrayList<View> activity_child_views = new ArrayList<>();
        final ArrayList<View> receiver_child_views = new ArrayList<>();
        final ArrayList<View> loaders_child_views = new ArrayList<>();
        final ArrayList<View> service_child_views = new ArrayList<>();
        final ArrayList<View> provider_child_views = new ArrayList<>();

        if (permissions != null && get_permissions) {
            for (final String s : permissions) {
                if (s == null) {
                    continue;
                }
                permission_child_views.add(getSingleItemView(assemblyView.getLinearLayout_permission(), s));
            }
        }
        if (activities != null && get_activities) {
            for (final ActivityInfo info : activities) {
                if (info == null) {
                    continue;
                }
                activity_child_views.add(getSingleItemView(assemblyView.getLinearLayout_activity(), info.name));
            }
        }
        if (receivers != null && get_receivers) {
            for (final ActivityInfo activityInfo : receivers) {
                if (activityInfo == null) {
                    continue;
                }
                receiver_child_views.add(getSingleItemView(assemblyView.getLinearLayout_receiver(), activityInfo.name));
            }
        }

        if (services != null && get_services) {
            for (final ServiceInfo serviceInfo : services) {
                if (serviceInfo == null) {
                    continue;
                }
                service_child_views.add(getSingleItemView(assemblyView.getLinearLayout_service(), serviceInfo.name));
            }
        }

        if (providers != null && get_providers) {
            for (final ProviderInfo providerInfo : providers) {
                if (providerInfo == null) {
                    continue;
                }
                provider_child_views.add(getSingleItemView(assemblyView.getLinearLayout_provider(), providerInfo.name));
            }
        }

        final Set<String> keys = static_receiver_bundle.keySet();
        if (get_static_loaders) {
            for (final String s : keys) {

                View static_loader_item_view = LayoutInflater.from(activity).inflate(R.layout.item_static_loader, assemblyView.getLinearLayout_loader(), false);
                ((TextView) static_loader_item_view.findViewById(R.id.static_loader_name)).setText(s);

                ViewGroup filter_views = static_loader_item_view.findViewById(R.id.static_loader_intents);
                List<String> filters = static_receiver_bundle.getStringArrayList(s);
                if (filters == null) continue;
                for (final String filter : filters) {
                    if (filter == null) {
                        continue;
                    }

                    View itemView = LayoutInflater.from(activity).inflate(R.layout.item_single_textview, null, false);
                    ((TextView) itemView.findViewById(R.id.item_textview)).setText(filter);

                    filter_views.addView(itemView);
                }
                loaders_child_views.add(static_loader_item_view);
            }
        }

        Global.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (get_permissions) {
                    assemblyView.setPermissionInfoToAllViews(permission_child_views);
                }
                if (get_activities) {
                    assemblyView.setActivityInfoToAllViews(activity_child_views);
                }
                if (get_receivers) {
                    assemblyView.setReceiverInfoToAllViews(receiver_child_views);
                }
                if (get_static_loaders) {
                    assemblyView.setStaticReceiverToAllViews(loaders_child_views);
                }
                if (get_services) {
                    assemblyView.setServiceInfoToAllViews(service_child_views);
                }
                if (get_providers) {
                    assemblyView.setProviderInfoToAllViews(provider_child_views);
                }
                callback.onViewsCreated();
            }
        });
    }

    public static void clearPackageInfoCache() {
        cache_wrapped_package_info.clear();
        cache_static_receivers.clear();
    }


    private View getSingleItemView(ViewGroup group, String text) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_single_textview, group, false);
        ((TextView) view.findViewById(R.id.item_textview)).setText(text);

        return view;
    }


    public interface CompletedCallback {
        void onViewsCreated();
    }
}

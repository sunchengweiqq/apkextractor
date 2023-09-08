package com.github.ghmxr.apkextractor.tasks;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.ghmxr.apkextractor.Global;
import com.github.ghmxr.apkextractor.ui.SignatureView;
import com.github.ghmxr.apkextractor.utils.EnvironmentUtil;

import java.util.concurrent.ConcurrentHashMap;

public class GetSignatureInfoTask extends Thread {

    private static final ConcurrentHashMap<String, String[]> sign_infos_cache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> md5_cache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> sha1_cache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> sha256_cache = new ConcurrentHashMap<>();

    private final Activity activity;
    private final PackageInfo packageInfo;
    private final SignatureView signatureView;
    private final CompletedCallback callback;

    public GetSignatureInfoTask(@NonNull Activity activity, @NonNull PackageInfo packageInfo, @NonNull SignatureView signatureView
            , @NonNull CompletedCallback callback) {
        super();
        this.activity = activity;
        this.packageInfo = packageInfo;
        this.signatureView = signatureView;
        this.callback = callback;
    }

    @Override
    public void run() {
        super.run();
        String[] sign_infos1 = sign_infos_cache.get(packageInfo.applicationInfo.sourceDir);
        String md5_1 = md5_cache.get(packageInfo.applicationInfo.sourceDir);
        String sha1_1 = sha1_cache.get(packageInfo.applicationInfo.sourceDir);
        String sha256_1 = sha256_cache.get(packageInfo.applicationInfo.sourceDir);

        if (sign_infos1 == null) {
            sign_infos1 = EnvironmentUtil.getAPKSignInfo(packageInfo.applicationInfo.sourceDir);
            sign_infos_cache.put(packageInfo.applicationInfo.sourceDir, sign_infos1);
        }

        if (md5_1 == null) {
            md5_1 = EnvironmentUtil.getSignatureMD5StringOfPackageInfo(activity.getPackageManager().getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, PackageManager.GET_SIGNATURES));
            md5_cache.put(packageInfo.applicationInfo.sourceDir, md5_1);
        }

        if (sha1_1 == null) {
            sha1_1 = EnvironmentUtil.getSignatureSHA1OfPackageInfo(activity.getPackageManager().getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, PackageManager.GET_SIGNATURES));
            sha1_cache.put(packageInfo.applicationInfo.sourceDir, sha1_1);
        }

        if (sha256_1 == null) {
            sha256_1 = EnvironmentUtil.getSignatureSHA256OfPackageInfo(activity.getPackageManager().getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir, PackageManager.GET_SIGNATURES));
            sha256_cache.put(packageInfo.applicationInfo.sourceDir, sha256_1);
        }

        final String[] sign_infos = sign_infos1;
        final String md5 = md5_1;
        final String sha1 = sha1_1;
        final String sha256 = sha256_1;

        Global.handler.post(new Runnable() {
            @Override
            public void run() {
                signatureView.getTv_sub_value().setText(sign_infos[0]);
                signatureView.getTv_iss_value().setText(sign_infos[1]);
                signatureView.getTv_serial_value().setText(sign_infos[2]);
                signatureView.getTv_start().setText(sign_infos[3]);
                signatureView.getTv_end().setText(sign_infos[4]);
                signatureView.getTv_md5().setText(md5);
                signatureView.getTv_sha1().setText(sha1);
                signatureView.getTv_sha256().setText(sha256);
                signatureView.getRoot().setVisibility(View.VISIBLE);
                callback.onCompleted();
            }
        });
    }


    public static void clearCache() {
        sign_infos_cache.clear();
        md5_cache.clear();
        sha1_cache.clear();
        sha256_cache.clear();
    }


    public interface CompletedCallback {
        void onCompleted();
    }
}

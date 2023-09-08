package com.github.ghmxr.apkextractor.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;

import com.github.ghmxr.apkextractor.Constants;
import com.github.ghmxr.apkextractor.R;
import com.github.ghmxr.apkextractor.items.AppItem;
import com.github.ghmxr.apkextractor.tasks.GetPackageInfoViewTask;
import com.github.ghmxr.apkextractor.tasks.GetSignatureInfoTask;
import com.github.ghmxr.apkextractor.tasks.HashTask;
import com.github.ghmxr.apkextractor.ui.AssemblyView;
import com.github.ghmxr.apkextractor.ui.LibraryView;
import com.github.ghmxr.apkextractor.ui.ToastManager;
import com.github.ghmxr.apkextractor.utils.SPUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AppDetailActivity extends BaseActivity implements View.OnClickListener {
    private AppItem appItem;
    private final BroadcastReceiver uninstall_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                    String data = intent.getDataString();
                    String package_name = data.substring(data.indexOf(":") + 1);
                    if (package_name.equalsIgnoreCase(appItem.getPackageName())) finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            appItem = savedInstanceState.getParcelable("appItem");
        } else {
            appItem = getIntent().getParcelableExtra(EXTRA_PARCELED_APP_ITEM);
        }
        if (appItem == null) {
            ToastManager.showToast(this, "AppItem info is null", Toast.LENGTH_SHORT);
            finish();
            return;
        }
        setContentView(R.layout.activity_app_detail);

        setSupportActionBar(findViewById(R.id.toolbar_app_detail));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final NestedScrollView nestedScrollView = findViewById(R.id.nsv);
        final FloatingActionButton floatingActionButton = findViewById(R.id.toTop);
        final ActionBar actionBar = getSupportActionBar();
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            int old_y;

            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {
                actionBar.setTitle(i1 > 0 ? appItem.getAppName() : "");
                if (i1 > old_y && old_y > 1500) {
                    floatingActionButton.show();
                } else {
                    floatingActionButton.hide();
                }
                old_y = i1;
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nestedScrollView.smoothScrollTo(0, 0);
            }
        });


        PackageInfo packageInfo = appItem.getPackageInfo();

        ((TextView) findViewById(R.id.app_detail_name)).setText(appItem.getAppName());
        ((TextView) findViewById(R.id.app_detail_version_name_title)).setText(appItem.getVersionName());
        ((ImageView) findViewById(R.id.app_detail_icon)).setImageDrawable(appItem.getIcon());

        ((TextView) findViewById(R.id.app_detail_package_name)).setText(appItem.getPackageName());
        ((TextView) findViewById(R.id.app_detail_version_name)).setText(appItem.getVersionName());
        ((TextView) findViewById(R.id.app_detail_version_code)).setText(String.valueOf(appItem.getVersionCode()));
        ((TextView) findViewById(R.id.app_detail_size)).setText(Formatter.formatFileSize(this, appItem.getSize()));
        ((TextView) findViewById(R.id.app_detail_install_time)).setText(SimpleDateFormat.getDateTimeInstance().format(new Date(packageInfo.firstInstallTime)));
        ((TextView) findViewById(R.id.app_detail_update_time)).setText(SimpleDateFormat.getDateTimeInstance().format(new Date(packageInfo.lastUpdateTime)));
        ((TextView) findViewById(R.id.app_detail_minimum_api)).setText(Build.VERSION.SDK_INT >= 24 ? String.valueOf(packageInfo.applicationInfo.minSdkVersion) : getResources().getString(R.string.word_unknown));
        ((TextView) findViewById(R.id.app_detail_target_api)).setText(String.valueOf(packageInfo.applicationInfo.targetSdkVersion));
        ((TextView) findViewById(R.id.app_detail_is_system_app)).setText(getResources().getString((appItem.getPackageInfo().applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 ? R.string.word_yes : R.string.word_no));
        ((TextView) findViewById(R.id.app_detail_path_value)).setText(appItem.getPackageInfo().applicationInfo.sourceDir);
        ((TextView) findViewById(R.id.app_detail_installer_name_value)).setText(appItem.getInstallSource());
        ((TextView) findViewById(R.id.app_detail_uid)).setText(String.valueOf(appItem.getPackageInfo().applicationInfo.uid));
        ((TextView) findViewById(R.id.app_detail_launcher_value)).setText(appItem.getLaunchingClass());

        if (SPUtil.getGlobalSharedPreferences(this).getBoolean(Constants.PREFERENCE_LOAD_NATIVE_FILE, Constants.PREFERENCE_LOAD_NATIVE_FILE_DEFAULT)) {
            try {
                LibraryView libraryView = (LibraryView) findViewById(R.id.libraryView);
                libraryView.setLibrary(appItem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        new GetPackageInfoViewTask(this, appItem.getPackageInfo(), findViewById(R.id.app_detail_assembly), new GetPackageInfoViewTask.CompletedCallback() {
            @Override
            public void onViewsCreated() {
                findViewById(R.id.app_detail_assembly).setVisibility(View.VISIBLE);
                findViewById(R.id.app_detail_card_pg).setVisibility(View.GONE);
            }
        }).start();

        if (SPUtil.getGlobalSharedPreferences(this).getBoolean(Constants.PREFERENCE_LOAD_APK_SIGNATURE, Constants.PREFERENCE_LOAD_APK_SIGNATURE_DEFAULT)) {
            findViewById(R.id.app_detail_signature_att).setVisibility(View.VISIBLE);
            findViewById(R.id.app_detail_sign_pg).setVisibility(View.VISIBLE);
            new GetSignatureInfoTask(this, appItem.getPackageInfo(), findViewById(R.id.app_detail_signature), new GetSignatureInfoTask.CompletedCallback() {
                @Override
                public void onCompleted() {
                    findViewById(R.id.app_detail_sign_pg).setVisibility(View.GONE);
                }
            }).start();
        }

        if (SPUtil.getGlobalSharedPreferences(this).getBoolean(Constants.PREFERENCE_LOAD_FILE_HASH, Constants.PREFERENCE_LOAD_FILE_HASH_DEFAULT)) {
            findViewById(R.id.app_detail_hash_att).setVisibility(View.VISIBLE);
            findViewById(R.id.app_detail_hash).setVisibility(View.VISIBLE);
            new HashTask(appItem.getFileItem(), HashTask.HashType.MD5, new HashTask.CompletedCallback() {
                @Override
                public void onHashCompleted(@NonNull String result) {
                    findViewById(R.id.detail_hash_md5_pg).setVisibility(View.GONE);
                    TextView tv_md5 = findViewById(R.id.detail_hash_md5_value);
                    tv_md5.setVisibility(View.VISIBLE);
                    tv_md5.setText(result);
                }
            }).start();
            new HashTask(appItem.getFileItem(), HashTask.HashType.SHA1, new HashTask.CompletedCallback() {
                @Override
                public void onHashCompleted(@NonNull String result) {
                    findViewById(R.id.detail_hash_sha1_pg).setVisibility(View.GONE);
                    TextView tv_sha1 = findViewById(R.id.detail_hash_sha1_value);
                    tv_sha1.setVisibility(View.VISIBLE);
                    tv_sha1.setText(result);
                }
            }).start();
            new HashTask(appItem.getFileItem(), HashTask.HashType.SHA256, new HashTask.CompletedCallback() {
                @Override
                public void onHashCompleted(@NonNull String result) {
                    findViewById(R.id.detail_hash_sha256_pg).setVisibility(View.GONE);
                    TextView tv_sha256 = findViewById(R.id.detail_hash_sha256_value);
                    tv_sha256.setVisibility(View.VISIBLE);
                    tv_sha256.setText(result);
                }
            }).start();
            new HashTask(appItem.getFileItem(), HashTask.HashType.CRC32, new HashTask.CompletedCallback() {
                @Override
                public void onHashCompleted(@NonNull String result) {
                    findViewById(R.id.detail_hash_crc32_pg).setVisibility(View.GONE);
                    TextView tv_crc32 = findViewById(R.id.detail_hash_crc32_value);
                    tv_crc32.setVisibility(View.VISIBLE);
                    tv_crc32.setText(result);
                }
            }).start();
        }

        try {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
            intentFilter.addDataScheme("package");
            registerReceiver(uninstall_receiver, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.app_detail_run_area: {
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage(appItem.getPackageName()));
                } catch (Exception e) {
                    ToastManager.showToast(AppDetailActivity.this, e.toString(), Toast.LENGTH_SHORT);
                }
            }
            break;
            case R.id.app_detail_export_area: {
                ToastManager.showToast(AppDetailActivity.this, appItem.getPackageName(), Toast.LENGTH_SHORT);
            }
            break;

            case R.id.app_detail_detail_area: {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", appItem.getPackageName(), null));
                startActivity(intent);
            }
            break;
            case R.id.app_detail_market_area: {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appItem.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    ToastManager.showToast(AppDetailActivity.this, e.toString(), Toast.LENGTH_SHORT);
                }
            }
            break;
            case R.id.app_detail_delete_area: {
                try {
                    Intent uninstall_intent = new Intent();
                    uninstall_intent.setAction(Intent.ACTION_DELETE);
                    uninstall_intent.setData(Uri.parse("package:" + appItem.getPackageName()));
                    startActivity(uninstall_intent);
                } catch (Exception e) {
                    ToastManager.showToast(AppDetailActivity.this, e.toString(), Toast.LENGTH_SHORT);
                }
            }
            break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            checkHeightAndFinish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void checkHeightAndFinish() {
        if (Build.VERSION.SDK_INT >= 28) {
            ActivityCompat.finishAfterTransition(this);
        } else {
            if (((AssemblyView) findViewById(R.id.app_detail_assembly)).getIsExpanded()) {
                finish();
            } else {
                ActivityCompat.finishAfterTransition(this);
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        try {
            unregisterReceiver(uninstall_receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                break;
            case android.R.id.home: {
                checkHeightAndFinish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.github.ghmxr.apkextractor.activities;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.app.AlertDialog;
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
import com.github.ghmxr.apkextractor.ui.SignatureView;
import com.github.ghmxr.apkextractor.ui.ToastManager;
import com.github.ghmxr.apkextractor.utils.EnvironmentUtil;
import com.github.ghmxr.apkextractor.utils.SPUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_app_detail));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle(appItem.getAppName());

//        ((CollapsingToolbarLayout)findViewById(R.id.coll)).
        final NestedScrollView nestedScrollView = findViewById(R.id.nsv);
        final FloatingActionButton floatingActionButton = findViewById(R.id.toTop);
        final ActionBar actionBar = getSupportActionBar();
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            int old_y;

            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int i, int i1, int i2, int i3) {
//                Log.e("111","i="+i+",i1="+i1+",i2="+i2+",i3="+i3);
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

        new GetPackageInfoViewTask(this, appItem.getPackageInfo(), (AssemblyView) findViewById(R.id.app_detail_assembly), new GetPackageInfoViewTask.CompletedCallback() {
            @Override
            public void onViewsCreated() {
                findViewById(R.id.app_detail_assembly).setVisibility(View.VISIBLE);
                findViewById(R.id.app_detail_card_pg).setVisibility(View.GONE);
            }
        }).start();

        if (SPUtil.getGlobalSharedPreferences(this).getBoolean(Constants.PREFERENCE_LOAD_APK_SIGNATURE, Constants.PREFERENCE_LOAD_APK_SIGNATURE_DEFAULT)) {
            findViewById(R.id.app_detail_signature_att).setVisibility(View.VISIBLE);
            findViewById(R.id.app_detail_sign_pg).setVisibility(View.VISIBLE);
            new GetSignatureInfoTask(this, appItem.getPackageInfo(), (SignatureView) findViewById(R.id.app_detail_signature), new GetSignatureInfoTask.CompletedCallback() {
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

            case R.id.app_detail_package_name_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_package_name)).getText().toString());
            }
            break;
            case R.id.app_detail_version_name_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_version_name)).getText().toString());
            }
            break;
            case R.id.app_detail_version_code_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_version_code)).getText().toString());
            }
            break;
            case R.id.app_detail_size_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_size)).getText().toString());
            }
            break;
            case R.id.app_detail_install_time_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_install_time)).getText().toString());
            }
            break;
            case R.id.app_detail_update_time_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_update_time)).getText().toString());
            }
            break;
            case R.id.app_detail_minimum_api_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_minimum_api)).getText().toString());
            }
            break;
            case R.id.app_detail_target_api_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_target_api)).getText().toString());
            }
            break;
            case R.id.app_detail_is_system_app_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_is_system_app)).getText().toString());
            }
            break;
            case R.id.detail_hash_md5: {
                final String value = ((TextView) findViewById(R.id.detail_hash_md5_value)).getText().toString();
                if (!TextUtils.isEmpty(value)) clip2ClipboardAndShowSnackbar(value);
            }
            break;
            case R.id.detail_hash_sha1: {
                final String value = ((TextView) findViewById(R.id.detail_hash_sha1_value)).getText().toString();
                if (!TextUtils.isEmpty(value)) clip2ClipboardAndShowSnackbar(value);
            }
            break;
            case R.id.detail_hash_sha256: {
                final String value = ((TextView) findViewById(R.id.detail_hash_sha256_value)).getText().toString();
                if (!TextUtils.isEmpty(value)) clip2ClipboardAndShowSnackbar(value);
            }
            break;
            case R.id.detail_hash_crc32: {
                final String value = ((TextView) findViewById(R.id.detail_hash_crc32_value)).getText().toString();
                if (!TextUtils.isEmpty(value)) clip2ClipboardAndShowSnackbar(value);
            }
            break;
            case R.id.app_detail_path_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_path_value)).getText().toString());
            }
            break;
            case R.id.app_detail_installer_name_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_installer_name_value)).getText().toString());
            }
            break;
            case R.id.app_detail_uid_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_uid)).getText().toString());
            }
            break;
            case R.id.app_detail_launcher_area: {
                clip2ClipboardAndShowSnackbar(((TextView) findViewById(R.id.app_detail_launcher_value)).getText().toString());
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                if (data.getData().getPath().toLowerCase().endsWith(appItem.getPackageName())) {
                    takePersistPermission(data.getData());
                } else {
                    showAttentionDialog(R.string.dialog_grant_attention_title, R.string.dialog_grant_warn, new Runnable() {
                        @Override
                        public void run() {
                            takePersistPermission(data.getData());
                        }
                    });
                }
            }
        }
    }

    private void clip2ClipboardAndShowSnackbar(String s) {
        try {
            ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            manager.setPrimaryClip(ClipData.newPlainText("message", s));
            Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.snack_bar_clipboard), Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takePersistPermission(Uri uri) {
        if (Build.VERSION.SDK_INT >= 19) {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        }
    }

    private void wrapGrantView(View v1, View v2, final String title, final String message, final Runnable confirmAction) {
        v1.setVisibility(View.VISIBLE);
        v1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGrantDialog(title, message, confirmAction);
            }
        });
        v2.setVisibility(View.GONE);
    }

    private void showGrantDialog(String title, String message, final Runnable confirmAction) {
        AlertDialog dialog = new AlertDialog.Builder(AppDetailActivity.this).setTitle(title)
                .setMessage(message)
                .setPositiveButton(getResources().getString(R.string.action_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmAction.run();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNeutralButton(getResources().getString(R.string.more_copy_package_names), null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnvironmentUtil.clip2Clipboard(String.valueOf(appItem.getPackageName()));
                ToastManager.showToast(AppDetailActivity.this, getResources().getString(R.string.snack_bar_clipboard), Toast.LENGTH_SHORT);
            }
        });
    }

    private void showAttentionDialog(int titleResId, int contentMessageId, final Runnable action_confirm) {
        new AlertDialog.Builder(AppDetailActivity.this).setTitle(getResources().getString(titleResId))
                .setMessage(getResources().getString(contentMessageId))
                .setPositiveButton(getResources().getString(R.string.action_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (action_confirm != null) {
                            action_confirm.run();
                        }
                    }
                })
                .setNegativeButton(getResources().getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
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
        if (Build.VERSION.SDK_INT >= 28) { //根布局项目太多时低版本Android会引发一个底层崩溃。版本号暂定28
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

package com.github.ghmxr.apkextractor.items;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.github.ghmxr.apkextractor.DisplayItem;
import com.github.ghmxr.apkextractor.MyApplication;
import com.github.ghmxr.apkextractor.R;
import com.github.ghmxr.apkextractor.utils.EnvironmentUtil;
import com.github.ghmxr.apkextractor.utils.FileUtil;

import java.io.File;

/**
 * 单个应用项的所有信息
 */
public class AppItem implements Parcelable, DisplayItem<AppItem> {


    public static final Creator<AppItem> CREATOR = new Creator<AppItem>() {
        @Override
        public AppItem createFromParcel(Parcel source) {
            return new AppItem(source);
        }

        @Override
        public AppItem[] newArray(int size) {
            return new AppItem[size];
        }
    };

    private final PackageInfo info;

    private final transient FileItem fileItem;

    /**
     * 程序名
     */
    private final String title;
    /**
     * 应用图标
     */
    private final transient Drawable drawable;

    /**
     * 应用大小
     */
    private final long size;

    private final String installSource;

    private final String launchingClass;

    //private String[]signatureInfos;

    //private HashMap<String, List<String>> static_receivers;


    /**
     * 初始化一个全新的AppItem
     *
     * @param context context实例，用来获取应用图标、名称等参数
     * @param info    PackageInfo实例，对应的本AppItem的信息
     */
    public AppItem(@NonNull Context context, @NonNull PackageInfo info) {
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        this.info = info;
//        this.fileItem = FileItem.createFileItemInstance(new File(info.applicationInfo.sourceDir));
        this.fileItem = new FileItem(new File(info.applicationInfo.sourceDir));
        this.title = packageManager.getApplicationLabel(info.applicationInfo).toString();
        this.size = FileUtil.getFileOrFolderSize(new File(info.applicationInfo.sourceDir));
        this.drawable = packageManager.getApplicationIcon(info.applicationInfo);
        String install_source = context.getResources().getString(R.string.word_unknown);
        try {
            final String installer_package_name = packageManager.getInstallerPackageName(info.packageName);
            final String installer_name = EnvironmentUtil.getAppNameByPackageName(context, installer_package_name);
            if (!TextUtils.isEmpty(installer_name)) {
                install_source = installer_name;
            } else {
                if (!TextUtils.isEmpty(installer_package_name)) {
                    install_source = installer_package_name;
                } else {
                    install_source = context.getResources().getString(R.string.word_unknown);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.installSource = install_source;

        String launchingClass = context.getResources().getString(R.string.word_unknown);
        try {
            Intent intent = packageManager.getLaunchIntentForPackage(info.packageName);
            if (intent == null)
                launchingClass = context.getResources().getString(R.string.word_none);
            else {
                launchingClass = intent.getComponent().getClassName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.launchingClass = launchingClass;
    }


    private AppItem(Parcel in) {
        title = in.readString();
        size = in.readLong();
        installSource = in.readString();
        launchingClass = in.readString();
        info = in.readParcelable(PackageInfo.class.getClassLoader());
        //static_receivers=in.readHashMap(HashMap.class.getClassLoader());

        assert info != null;
//        fileItem = FileItem.createFileItemInstance(info.applicationInfo.sourceDir);
        fileItem = new FileItem(info.applicationInfo.sourceDir);
        drawable = MyApplication.getApplication().getPackageManager().getApplicationIcon(info.applicationInfo);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeLong(size);
        dest.writeString(installSource);
        dest.writeString(launchingClass);
        dest.writeParcelable(info, 0);
    }

    public Drawable getIconDrawable() {
        return drawable;
    }

    public String getTitle() {
        return title + "(" + getVersionName() + ")";
    }

    public String getDescription() {
        return info.packageName;
    }

    public boolean isRedMarked() {
        return (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
    }

    /**
     * 获取应用图标
     */
    public Drawable getIcon() {
        return drawable;
    }

    /**
     * 获取应用名称
     */
    public String getAppName() {
        return title;
    }

    /**
     * 获取包名
     */
    public String getPackageName() {
        return info.packageName;
    }

    /**
     * 获取应用源路径
     */
    public String getSourcePath() {
        return String.valueOf(info.applicationInfo.sourceDir);
    }

    /**
     * 获取应用大小（源文件），单位字节
     */
    public long getSize() {
        return size;
    }

    /**
     * 获取应用版本名称
     */
    public String getVersionName() {
        return info.versionName;
    }

    /**
     * 获取应用版本号
     */
    public int getVersionCode() {
        return info.versionCode;
    }

    public PackageInfo getPackageInfo() {
        return info;
    }

    public String getInstallSource() {
        return installSource;
    }

    public String getLaunchingClass() {
        return launchingClass;
    }

    public FileItem getFileItem() {
        return fileItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

}

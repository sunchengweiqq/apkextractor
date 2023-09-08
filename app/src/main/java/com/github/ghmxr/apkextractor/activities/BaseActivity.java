package com.github.ghmxr.apkextractor.activities;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;

import com.github.ghmxr.apkextractor.Constants;
import com.github.ghmxr.apkextractor.utils.SPUtil;

import java.lang.reflect.Method;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    /**
     *
     */
    public static final String EXTRA_PARCELED_APP_ITEM = "app_item";

    public static final String EXTRA_PACKAGE_NAME = "package_name";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setAndRefreshLanguage();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    public void setAndRefreshLanguage() {
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        int value = SPUtil.getGlobalSharedPreferences(this).getInt(Constants.PREFERENCE_LANGUAGE, Constants.PREFERENCE_LANGUAGE_DEFAULT);
        Locale locale = null;
        switch (value) {
            default:
                break;
            case Constants.LANGUAGE_FOLLOW_SYSTEM:
                locale = Locale.getDefault();
                break;
            case Constants.LANGUAGE_CHINESE:
                locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case Constants.LANGUAGE_ENGLISH:
                locale = Locale.ENGLISH;
                break;
        }
        if (locale == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, metrics);
    }
}

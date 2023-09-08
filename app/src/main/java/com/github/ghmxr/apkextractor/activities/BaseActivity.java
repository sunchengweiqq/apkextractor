package com.github.ghmxr.apkextractor.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String EXTRA_PARCELED_APP_ITEM = "app_item";

    public static final String EXTRA_PACKAGE_NAME = "package_name";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

package com.github.ghmxr.apkextractor.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.github.ghmxr.apkextractor.Constants;
import com.github.ghmxr.apkextractor.R;
import com.github.ghmxr.apkextractor.adapters.MyPagerAdapter;
import com.github.ghmxr.apkextractor.fragments.AppFragment;
import com.github.ghmxr.apkextractor.fragments.ImportFragment;
import com.github.ghmxr.apkextractor.ui.AppItemSortConfigDialog;
import com.github.ghmxr.apkextractor.ui.ImportItemSortConfigDialog;
import com.github.ghmxr.apkextractor.ui.SortConfigDialogCallback;
import com.github.ghmxr.apkextractor.utils.EnvironmentUtil;
import com.github.ghmxr.apkextractor.utils.SPUtil;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener, CompoundButton.OnCheckedChangeListener
        {

    private final AppFragment appFragment = new AppFragment();
    private final ImportFragment importFragment = new ImportFragment();

    private int currentSelection = 0;
    private boolean isSearchMode = false;
    private EditText edit_search;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        } catch (Exception e) {
            e.printStackTrace();
        }


        TabLayout tabLayout = findViewById(R.id.main_tablayout);
        ViewPager viewPager = findViewById(R.id.main_viewpager);


        View view = LayoutInflater.from(this).inflate(R.layout.actionbar_search, null);
        final View cancelView = view.findViewById(R.id.actionbar_main_cancel);
        edit_search = view.findViewById(R.id.actionbar_main_edit);
        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) return;
                cancelView.setVisibility(s.length() > 0 ? View.VISIBLE : View.INVISIBLE);
                if (!isSearchMode) return;
                appFragment.updateSearchModeKeywords(s.toString());
                importFragment.updateSearchModeKeywords(s.toString());
            }
        });
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit_search.setText("");
            }
        });
        try {
            getSupportActionBar().setCustomView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }

        viewPager.setAdapter(new MyPagerAdapter(this, getSupportFragmentManager(), appFragment, importFragment));
        tabLayout.setupWithViewPager(viewPager, true);
        viewPager.addOnPageChangeListener(this);

        if (Build.VERSION.SDK_INT >= 23
                && PermissionChecker.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
    }

    @Override
    public void onPageSelected(int i) {
        this.currentSelection = i;
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length == 0) return;
            if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                sendBroadcast(new Intent(Constants.ACTION_REFRESH_IMPORT_ITEMS_LIST));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        //setIconEnable(menu,true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == android.R.id.home) {
            checkAndExit();
        }

        if (id == R.id.action_search) {
            openSearchMode();
        }

        if (id == R.id.action_view) {
            if (isSearchMode) return false;
            final SharedPreferences settings = SPUtil.getGlobalSharedPreferences(this);
            final SharedPreferences.Editor editor = settings.edit();
            if (currentSelection == 0) {
                final int mode_app = settings.getInt(Constants.PREFERENCE_MAIN_PAGE_VIEW_MODE, Constants.PREFERENCE_MAIN_PAGE_VIEW_MODE_DEFAULT);
                final int result_app = mode_app == 0 ? 1 : 0;
                editor.putInt(Constants.PREFERENCE_MAIN_PAGE_VIEW_MODE, result_app);
                editor.apply();
                appFragment.setViewMode(result_app);
            } else if (currentSelection == 1) {
                final int mode_pak = settings.getInt(Constants.PREFERENCE_MAIN_PAGE_VIEW_MODE_IMPORT, Constants.PREFERENCE_MAIN_PAGE_VIEW_MODE_IMPORT_DEFAULT);
                final int result_pak = mode_pak == 0 ? 1 : 0;
                editor.putInt(Constants.PREFERENCE_MAIN_PAGE_VIEW_MODE_IMPORT, result_pak);
                editor.apply();
                importFragment.setViewMode(result_pak);
            }
        }

        if (id == R.id.action_sort) {
            if (currentSelection == 0) {
                AppItemSortConfigDialog appItemSortConfigDialog = new AppItemSortConfigDialog(this, new SortConfigDialogCallback() {
                    @Override
                    public void onOptionSelected(int value) {
                        appFragment.sortGlobalListAndRefresh(value);
                    }
                });
                appItemSortConfigDialog.show();
            } else if (currentSelection == 1) {
                ImportItemSortConfigDialog importItemSortConfigDialog = new ImportItemSortConfigDialog(this, new SortConfigDialogCallback() {
                    @Override
                    public void onOptionSelected(int value) {
                        importFragment.sortGlobalListAndRefresh(value);
                    }
                });
                importItemSortConfigDialog.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        checkAndExit();
    }

    private static final int REQUEST_CODE_SETTINGS = 0;
    private static final int REQUEST_CODE_RECEIVING_FILES = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            default: {
                appFragment.onActivityResult(requestCode, resultCode, data);
                importFragment.onActivityResult(requestCode, resultCode, data);
            }
            break;
            case REQUEST_CODE_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    //recreate();
                    finish();
                    startActivity(new Intent(this, MainActivity.class));
                }
            }
            break;
            case REQUEST_CODE_RECEIVING_FILES: {
                if (resultCode == RESULT_OK) {
                    sendBroadcast(new Intent(Constants.ACTION_REFRESH_IMPORT_ITEMS_LIST));
                }
            }
            break;
        }
    }

    private void openSearchMode() {
        isSearchMode = true;
        setActionbarDisplayCustomView(true);
        setMenuVisible(false);
        appFragment.setSearchMode(true);
        importFragment.setSearchMode(true);
        EnvironmentUtil.showInputMethod(edit_search);
    }

    private void closeSearchMode() {
        isSearchMode = false;
        setMenuVisible(true);
        edit_search.setText("");
        setActionbarDisplayCustomView(false);
        appFragment.setSearchMode(false);
        importFragment.setSearchMode(false);
        EnvironmentUtil.hideInputMethod(this);
    }

    private void setMenuVisible(boolean b) {
        if (menu == null) return;
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(b);
        }
    }

    private void setActionbarDisplayCustomView(boolean b) {
        try {
            getSupportActionBar().setDisplayShowCustomEnabled(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndExit() {
        if (isSearchMode) {
            closeSearchMode();
            return;
        }
        switch (currentSelection) {
            default:
                break;
            case 0: {
                if (appFragment.isMultiSelectMode()) {
                    appFragment.closeMultiSelectMode();
                    return;
                }
            }
            break;
            case 1: {
                if (importFragment.isMultiSelectMode()) {
                    importFragment.closeMultiSelectMode();
                    return;
                }
            }
            break;
        }
        finish();
    }
}

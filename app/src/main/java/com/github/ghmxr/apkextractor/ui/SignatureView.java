package com.github.ghmxr.apkextractor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.ghmxr.apkextractor.R;

public class SignatureView extends LinearLayout {

    private final ViewGroup root;

    private final TextView tv_sub_value;
    private final TextView tv_iss_value;
    private final TextView tv_serial_value;
    private final TextView tv_start;
    private final TextView tv_end;
    private final TextView tv_md5;
    private final TextView tv_sha1;
    private final TextView tv_sha256;

    private final LinearLayout linearLayout_sub;
    private final LinearLayout linearLayout_iss;
    private final LinearLayout linearLayout_serial;
    private final LinearLayout linearLayout_start;
    private final LinearLayout linearLayout_end;
    private final LinearLayout linearLayout_md5;
    private final LinearLayout linearLayout_sha1;
    private final LinearLayout linearLayout_sha256;


    public SignatureView(Context context) {
        this(context, null);
    }

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignatureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.layout_card_signature, this);
        root = findViewById(R.id.detail_signature_root);
        tv_sub_value = findViewById(R.id.detail_signature_sub_value);
        tv_iss_value = findViewById(R.id.detail_signature_iss_value);
        tv_serial_value = findViewById(R.id.detail_signature_serial_value);
        tv_start = findViewById(R.id.detail_signature_start_value);
        tv_end = findViewById(R.id.detail_signature_end_value);
        tv_md5 = findViewById(R.id.detail_signature_md5_value);
        tv_sha1 = findViewById(R.id.detail_signature_sha1_value);
        tv_sha256 = findViewById(R.id.detail_signature_sha256_value);

        linearLayout_sub = findViewById(R.id.detail_signature_sub);
        linearLayout_iss = findViewById(R.id.detail_signature_iss);
        linearLayout_serial = findViewById(R.id.detail_signature_serial);
        linearLayout_start = findViewById(R.id.detail_signature_start);
        linearLayout_end = findViewById(R.id.detail_signature_end);
        linearLayout_md5 = findViewById(R.id.detail_signature_md5);
        linearLayout_sha1 = findViewById(R.id.detail_signature_sha1);
        linearLayout_sha256 = findViewById(R.id.detail_signature_sha256);
    }

    public TextView getTv_sub_value() {
        return tv_sub_value;
    }

    public TextView getTv_iss_value() {
        return tv_iss_value;
    }

    public TextView getTv_serial_value() {
        return tv_serial_value;
    }

    public TextView getTv_start() {
        return tv_start;
    }

    public TextView getTv_end() {
        return tv_end;
    }

    public TextView getTv_md5() {
        return tv_md5;
    }

    public TextView getTv_sha1() {
        return tv_sha1;
    }

    public TextView getTv_sha256() {
        return tv_sha256;
    }


    public ViewGroup getRoot() {
        return root;
    }
}

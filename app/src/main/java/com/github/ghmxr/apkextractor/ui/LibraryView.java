package com.github.ghmxr.apkextractor.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ghmxr.apkextractor.R;
import com.github.ghmxr.apkextractor.items.AppItem;
import com.github.ghmxr.apkextractor.items.FileItem;
import com.github.ghmxr.apkextractor.tasks.GetApkLibraryTask;
import com.github.ghmxr.apkextractor.utils.EnvironmentUtil;
import com.google.android.material.snackbar.Snackbar;

public class LibraryView extends RelativeLayout {

    View loadingView;
    RecyclerView recyclerView;
    View headArea;
    ImageView arrow;
    TextView title;
    TextView soCardText;

    public LibraryView(Context context) {
        this(context, null);
    }

    public LibraryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LibraryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.layout_card_library, this);
        loadingView = findViewById(R.id.library_pg);
        recyclerView = findViewById(R.id.libraries_rv);
        headArea = findViewById(R.id.native_library_head);
        arrow = findViewById(R.id.native_library_arrow);
        title = findViewById(R.id.native_library_title);
        soCardText = findViewById(R.id.soName);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
    }

    public interface LibraryInfoCallback {
        void onLibraryInfoFilled(GetApkLibraryTask.LibraryInfo libraryInfo);
    }

    private LibraryInfoCallback libraryInfoCallback;


    public void setLibrary(AppItem appItem) {
        refreshLibraryToView(appItem.getFileItem());
    }


    private void refreshLibraryToView(FileItem fileItem) {
        findViewById(R.id.detail_signature_root).setVisibility(VISIBLE);
        new GetApkLibraryTask(fileItem, new GetApkLibraryTask.GetApkLibraryCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onApkLibraryGet(GetApkLibraryTask.LibraryInfo libraryInfo) {
                loadingView.setVisibility(GONE);
                if (libraryInfo == null || libraryInfo.getAllLibraryNames().isEmpty()) {
                    setVisibility(GONE);
                    return;
                } else {
                    recyclerView.setAdapter(new LibraryViewListAdapter(libraryInfo));
                }
                final int size = libraryInfo.getAllLibraryNames().size();
                title.setText(getContext().getResources().getString(R.string.activity_detail_native_library) + "(" + size
                        + getContext().getResources().getString(R.string.unit_item) + ")");
                if (size > 20) {
                    recyclerView.setVisibility(GONE);
                    arrow.setRotation(0F);
                } else {
                    recyclerView.setVisibility(VISIBLE);
                    arrow.setRotation(90F);
                }
                headArea.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (recyclerView.getVisibility() == VISIBLE) {
                            recyclerView.setVisibility(GONE);
                            arrow.setRotation(0F);
                        } else {
                            recyclerView.setVisibility(VISIBLE);
                            arrow.setRotation(90F);
                        }
                    }
                });
                GetApkLibraryTask.LibraryType showingLibraryType = EnvironmentUtil.getShowingLibraryType(libraryInfo);
                if (showingLibraryType != null) {
                    soCardText.setVisibility(VISIBLE);
                    soCardText.setText(showingLibraryType.toString());
                    soCardText.setBackgroundResource(GetApkLibraryTask.is64BitAbi(showingLibraryType) ? R.drawable.shape_card_64bit_abi
                            : R.drawable.shape_card_32bit_abi);
                }

                final LibraryInfoCallback callback = LibraryView.this.libraryInfoCallback;
                if (callback != null) {
                    callback.onLibraryInfoFilled(libraryInfo);
                }
            }
        }).start();
    }

    private class LibraryViewListAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final GetApkLibraryTask.LibraryInfo libraryInfo;
        private final String[] libraryNames;

        public LibraryViewListAdapter(GetApkLibraryTask.LibraryInfo libraryInfo) {
            this.libraryInfo = libraryInfo;
            libraryNames = libraryInfo.getAllLibraryNames().toArray(new String[0]);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_library, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            final String libraryName = libraryNames[i];
            viewHolder.tv1.setText(libraryName);
            viewHolder.tv2.setText(getDisplayLibraryInfo(libraryName));
            if (i == getItemCount() - 1) {
                viewHolder.dividingLine.setVisibility(INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return libraryNames.length;
        }

        private String getDisplayLibraryInfo(String libraryName) {
            StringBuilder stringBuilder = new StringBuilder();
            for (GetApkLibraryTask.LibraryType type : libraryInfo.getLibraryTypes(libraryName)) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(",");
                }
                stringBuilder.append(type.toString());
                stringBuilder.append("(");
                stringBuilder.append(Formatter.formatFileSize(getContext(), libraryInfo.getLibraryItemSize(type, libraryName)));
                stringBuilder.append(")");
            }
            return stringBuilder.toString();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv1, tv2;
        View dividingLine;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(R.id.lib_name);
            tv2 = itemView.findViewById(R.id.lib_type);
            dividingLine = itemView.findViewById(R.id.dividingLine);
        }
    }

}

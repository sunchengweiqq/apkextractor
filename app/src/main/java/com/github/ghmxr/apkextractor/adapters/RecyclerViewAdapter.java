package com.github.ghmxr.apkextractor.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.ghmxr.apkextractor.DisplayItem;
import com.github.ghmxr.apkextractor.R;
import com.github.ghmxr.apkextractor.fragments.AppFragment;
import com.github.ghmxr.apkextractor.items.AppItem;
import com.github.ghmxr.apkextractor.utils.EnvironmentUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RecyclerViewAdapter<T extends DisplayItem<T>> extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final Activity activity;
    private final RecyclerView recyclerView;
    private final ArrayList<AppItem> data = new ArrayList<>();
    private final ListAdapterOperationListener<AppItem> listener;
    private final HashSet<AppItem> selectedItems = new HashSet<>();
    private int mode;
    private String highlightKeyword = null;

    public RecyclerViewAdapter(@NonNull Activity activity, @NonNull RecyclerView recyclerView, @Nullable List<AppItem> data, int viewMode,
                               @NonNull AppFragment listener) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        if (data != null) this.data.addAll(data);
        this.listener = listener;
        this.mode = viewMode;
        setLayoutManagerAndView(mode);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(i == 0 ? R.layout.item_app_info_linear
                : R.layout.item_app_info_grid, viewGroup, false), i);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final AppItem item = data.get(viewHolder.getAdapterPosition());
        viewHolder.title.setTextColor(activity.getResources().getColor((item.isRedMarked() ?
                R.color.colorSystemAppTitleColor : R.color.colorHighLightText)));
        try {
            viewHolder.title.setText(EnvironmentUtil.getSpannableString(String.valueOf(item.getTitle()), highlightKeyword, Color.parseColor("#3498db")));
        } catch (Exception e) {
            e.printStackTrace();
            viewHolder.title.setText(String.valueOf(item.getTitle()));
        }
        viewHolder.icon.setImageDrawable(item.getIconDrawable());
        if (viewHolder.getViewType() == 0) {
            try {
                viewHolder.description.setText(EnvironmentUtil.getSpannableString(item.getDescription(), highlightKeyword, Color.parseColor("#3498db")));
            } catch (Exception e) {
                e.printStackTrace();
                viewHolder.description.setText(String.valueOf(item.getDescription()));
            }
            viewHolder.right.setText(Formatter.formatFileSize(activity, item.getSize()));

        } else if (viewHolder.getViewType() == 1) {

            viewHolder.root.setBackgroundColor(activity.getResources().getColor(R.color.colorCardArea));
        }

        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null)
                    listener.onItemClicked(item, viewHolder, viewHolder.getAdapterPosition());

            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mode;
    }

    public void setData(@Nullable List<AppItem> data) {
        setData(data, false);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(@Nullable List<AppItem> data, boolean clear) {
        this.data.clear();
        if (clear) {

            selectedItems.clear();
        }
        if (data != null) this.data.addAll(data);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setHighlightKeyword(String keyword) {
        this.highlightKeyword = keyword;
        notifyDataSetChanged();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setLayoutManagerAndView(int mode) {
        if (mode == 1) {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 4);
            recyclerView.setLayoutManager(gridLayoutManager);
            this.mode = 1;
        } else {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(linearLayoutManager);
            this.mode = 0;
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final int viewType;
        public ImageView icon;
        TextView title;
        TextView description;
        TextView right;
        View root;

        ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            root = itemView.findViewById(R.id.item_app_root);
            icon = itemView.findViewById(R.id.item_app_icon);
            title = itemView.findViewById(R.id.item_app_title);
            if (viewType == 0) {
                description = itemView.findViewById(R.id.item_app_description);
                right = itemView.findViewById(R.id.item_app_right);
            }
        }

        int getViewType() {
            return viewType;
        }
    }

    public interface ListAdapterOperationListener<T> {
        void onItemClicked(AppItem item, ViewHolder viewHolder, int position);

    }
}

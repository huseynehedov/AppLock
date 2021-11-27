package com.huseyn.myapplock.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huseyn.myapplock.R;

public class AppListViewHolder extends RecyclerView.ViewHolder {
    public TextView appName;
    public ImageView appIcon, lockApp;

    public AppListViewHolder(@NonNull View itemView) {
        super(itemView);

        appName = itemView.findViewById(R.id.app_name);
        appIcon = itemView.findViewById(R.id.appIcon);
        lockApp = itemView.findViewById(R.id.app_status);
    }
}

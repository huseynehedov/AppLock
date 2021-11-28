package com.huseyn.myapplock.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huseyn.myapplock.R;
import com.huseyn.myapplock.model.MyItem;
import com.huseyn.myapplock.utils.SharedPrefUtil;
import com.huseyn.myapplock.viewholder.AppListViewHolder;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListViewHolder> {
    private Context context;
    private List<MyItem> apps;
    private List<String> lockedApps ;

    public AppListAdapter(Context context, List<MyItem> apps) {
        this.context = context;
        this.apps = apps;

       lockedApps = SharedPrefUtil.getInstance(context).getStringList();

    }

    @NonNull
    @Override
    public AppListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_view, parent, false);
        return new AppListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppListViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.appName.setText(apps.get(position).getName());
        holder.appIcon.setImageDrawable(apps.get(position).getIcon());

        if (apps.get(position).isAppStatus()){
            holder.lockApp.setImageResource(R.drawable.lock_24);
        }else{
            holder.lockApp.setImageResource(R.drawable.lock_open_24);
        }

        holder.lockApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (apps.get(position).isAppStatus()){
                    apps.get(position).setAppStatus(false);
                    holder.lockApp.setImageResource(R.drawable.lock_open_24);
                    //TODO("APPLY CHANGES ")
                    lockedApps.remove(apps.get(position).getPackageName());
                    System.out.println("Deleted PackageName: " +  apps.get(position).getPackageName());
                    System.out.println(apps.get(position).getName());
                    SharedPrefUtil.getInstance(context).putStringList(lockedApps);
                }else{
                    apps.get(position).setAppStatus(true);
                    holder.lockApp.setImageResource(R.drawable.lock_24);
                    //TODO("APPLY CHANGES ")
                    lockedApps.add(apps.get(position).getPackageName());
                    System.out.println("Added PackageName: " +  apps.get(position).getPackageName());
                    SharedPrefUtil.getInstance(context).putStringList(lockedApps);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

}

package com.huseyn.myapplock.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huseyn.myapplock.R;
import com.huseyn.myapplock.adapter.AppListAdapter;
import com.huseyn.myapplock.model.MyItem;
import com.huseyn.myapplock.utils.SharedPrefUtil;
import com.huseyn.myapplock.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mainRV;
    private FloatingActionButton fabOk;
    private PackageManager packageManager;
    private Toolbar toolbar;
    LinearLayout layoutPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layoutPermissions = findViewById(R.id.layout_permissions);
        fabOk = findViewById(R.id.fab_ok);

        initView();
        initToolbar();
    }

    private void initView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppListAdapter adapter = new AppListAdapter(this, getApplications());
        recyclerView.setAdapter(adapter);
    }

    private List<MyItem> getApplications() {
        List<String> lockedApps = SharedPrefUtil.getInstance(this).getStringList();
        List<MyItem> items = new ArrayList<>();
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo info: infos){
            ActivityInfo activityInfo = info.activityInfo;

            if (!lockedApps.isEmpty()){
                if (lockedApps.contains(activityInfo.packageName)){
                    items.add(new MyItem(activityInfo.loadIcon(packageManager),
                            activityInfo.loadLabel(packageManager).toString(), activityInfo.packageName, true));
                }else{
                    items.add(new MyItem(activityInfo.loadIcon(packageManager),
                            activityInfo.loadLabel(packageManager).toString(), activityInfo.packageName, false));
                }
            }else{
                items.add(new MyItem(activityInfo.loadIcon(packageManager),
                        activityInfo.loadLabel(packageManager).toString(), activityInfo.packageName, false));
            }

        }

        return items;
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toobar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Applications");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home){
            finish();
        }

        return true;
    }

    public void setPermission(View view) {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    @Override
    protected void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if (Utils.PermissionCheck(this)){
                layoutPermissions.setVisibility(View.GONE);
            }else{
                layoutPermissions.setVisibility(View.VISIBLE);
            }
        }
        super.onResume();
    }

}
package com.huseyn.myapplock.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.huseyn.myapplock.R;
import com.huseyn.myapplock.adapter.AppListAdapter;
import com.huseyn.myapplock.model.MyItem;
import com.huseyn.myapplock.utils.SharedPrefUtil;

import java.util.ArrayList;
import java.util.List;

public class AppListActivity extends AppCompatActivity {
    private RecyclerView mainRV;
    private FloatingActionButton fabOk;
    private PackageManager packageManager;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        fabOk = findViewById(R.id.fab_ok);

        initView();
        initToolbar();

        fabOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO("SAVE RESULTS")
            }
        });
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
        packageManager = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo info: infos){
            ActivityInfo activityInfo = info.activityInfo;

            if (lockedApps.contains(activityInfo.packageName)) {
                items.add(new MyItem(activityInfo.loadIcon(packageManager),
                        activityInfo.loadLabel(packageManager).toString(), activityInfo.packageName, true));
            } else {
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

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.home){
//            finish();
//        }
//
//        return true;
//    }

}
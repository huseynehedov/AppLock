package com.huseyn.myapplock.service;

import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.huseyn.myapplock.lock.PinLockActivity;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LockWorker extends Worker {
    private String chrome = "com.android.chrome";
    private String chromeName = "Chrome";
    private String lastPackageName = "";
    private String myPackageName = "com.huseyn.myapplock";
    public static final String KEY_RECEIVER = "LockResult";
    private static AtomicBoolean started = new AtomicBoolean(false);
    public static ConcurrentHashMap<String, Boolean> unlockedApps = new ConcurrentHashMap<>();

    public LockWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (started.get()){
            return Result.success();
        }
        started.set(true);
        System.out.println("Started Worker");
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String topPackageName = "";
            ActivityManager mActivityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    UsageStatsManager mUsageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
                    List<UsageStats> stats =
                            mUsageStatsManager.queryUsageStats(
                                    UsageStatsManager.INTERVAL_DAILY,
                                    System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
                                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
                    if (stats != null) {
                        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                        for (UsageStats usageStats : stats) {
                            mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                        }
                        if (!mySortedMap.isEmpty()) {
                            topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        }
                    } else {
                        topPackageName = mActivityManager.getRunningAppProcesses().get(0).processName;
                    }
                } else {
                    topPackageName = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("TopPackageName " + topPackageName);

            if (!topPackageName.equals(lastPackageName)){
                if (topPackageName.equals(chrome) && !unlockedApps.containsKey(chrome)){
                    System.out.println("Open Activity");
                    Intent intent = new Intent(getApplicationContext(), PinLockActivity.class);
                    intent.putExtra("LockResult", new LockResult(chrome));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_MULTIPLE_TASK);
                    getApplicationContext().startActivity(intent);
                }
            }

            lastPackageName = topPackageName;

        }
    }

    public class LockResult extends ResultReceiver {
        private final String packageName;

        public LockResult(String packageName) {
            super(null);
            this.packageName = packageName;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode != PinLockActivity.RESULT_OK){
                return;
            }
            String message = resultData.getString(PinLockActivity.RESULT_KEY);
            if (message.equals("true")){
                unlockedApps.put(packageName, true);
            }
        }
    }
}

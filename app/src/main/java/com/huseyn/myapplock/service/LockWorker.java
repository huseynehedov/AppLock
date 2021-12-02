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
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.huseyn.myapplock.lock.FingerprintActivity;
import com.huseyn.myapplock.lock.PatternLockActivity;
import com.huseyn.myapplock.lock.PinLockActivity;
import com.huseyn.myapplock.utils.SharedPrefUtil;

import java.net.URISyntaxException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LockWorker extends Worker {
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
                Thread.sleep(100);
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

            List<String>  lockedAppList = SharedPrefUtil.getInstance(getApplicationContext()).getStringList();

            if (!topPackageName.equals(lastPackageName)){
                if (lockedAppList.contains(topPackageName) && !unlockedApps.containsKey(topPackageName)){
                    System.out.println("Open Activity");
                    Intent intent ;
                    String lockType = SharedPrefUtil.getInstance(getApplicationContext()).getString("LockType");
                    System.out.println("LockType: " + lockType);
                    if (lockType.equals("Pin")){
                        intent  = new Intent(getApplicationContext(), PinLockActivity.class);
                    }else if (lockType.equals("Fingerprint")){
                        intent  = new Intent(getApplicationContext(), FingerprintActivity.class);
                    }else{
                        intent  = new Intent(getApplicationContext(), PatternLockActivity.class);
                    }
                    intent.putExtra("LockResult", new LockResult(topPackageName));
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

            String lockType = SharedPrefUtil.getInstance(getApplicationContext()).getString("LockType");
            System.out.println("LockType: " + lockType);

            if (lockType.equals("Pin")){

                if (resultCode != PinLockActivity.RESULT_OK){
                    return;
                }
                String message = resultData.getString(PinLockActivity.RESULT_KEY);
                if (message.equals("true")){
                    unlockedApps.put(packageName, true);
                }

            }else if (lockType.equals("Fingerprint")){

                if (resultCode != FingerprintActivity.RESULT_OK){
                    return;
                }
                String message = resultData.getString(FingerprintActivity.RESULT_KEY);
                if (message.equals("true")){
                    unlockedApps.put(packageName, true);
                }

            }else{
                if (resultCode != PatternLockActivity.RESULT_OK){
                    return;
                }
                String message = resultData.getString(PatternLockActivity.RESULT_KEY);
                if (message.equals("true")){
                    unlockedApps.put(packageName, true);
                }
            }
        }
    }

    @Override
    public void onStopped() {
        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(LockWorker.class).build();
        WorkManager
                .getInstance(getApplicationContext())
                .enqueue(uploadWorkRequest);
    }
}

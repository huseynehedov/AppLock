package com.huseyn.myapplock.receiver;

import static android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.huseyn.myapplock.lock.FingerprintActivity;
import com.huseyn.myapplock.lock.PatternLockActivity;
import com.huseyn.myapplock.lock.PinLockActivity;
import com.huseyn.myapplock.service.LockWorker;
import com.huseyn.myapplock.utils.SharedPrefUtil;

public class ScreenLockReceiver extends BroadcastReceiver {
    public static final String TAG = "ScreenLockReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        String log = sb.toString();
        System.out.println(log);
        Log.d(TAG, log);
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            LockWorker.unlockedApps.clear();
        }else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            Intent intent2 ;
            String lockType = SharedPrefUtil.getInstance(context).getString("LockType");
            if (lockType.equals("Pin")){
                intent2  = new Intent(context, PinLockActivity.class);
            }else if (lockType.equals("Fingerprint")){
                intent2  = new Intent(context, FingerprintActivity.class);
            }else{
                intent2  = new Intent(context, PatternLockActivity.class);
            }
            intent2.putExtra("LockResult", new LockResult(context));
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_MULTIPLE_TASK);
            context.startActivity(intent2);
        }
    }

    public class LockResult extends ResultReceiver {
        Context context;

        public LockResult(Context context) {
            super(null);
            this.context = context;
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            String lockType = SharedPrefUtil.getInstance(context).getString("LockType");

            if (lockType.equals("Pin")) {

                if (resultCode != PinLockActivity.RESULT_OK) {
                    return;
                }
                String message = resultData.getString(PinLockActivity.RESULT_KEY);
                if (!message.equals("true")) {
                    Intent intent = new Intent(context, PinLockActivity.class);
                    intent.putExtra("LockResult", new LockResult(context));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_MULTIPLE_TASK);
                    context.startActivity(intent);
                }

            } else if (lockType.equals("Fingerprint")) {

                if (resultCode != FingerprintActivity.RESULT_OK) {
                    return;
                }
                String message = resultData.getString(FingerprintActivity.RESULT_KEY);
                if (!message.equals("true")) {
                    Intent intent = new Intent(context, FingerprintActivity.class);
                    intent.putExtra("LockResult", new LockResult(context));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_MULTIPLE_TASK);
                    context.startActivity(intent);
                }

            } else {

                if (resultCode != PatternLockActivity.RESULT_OK) {
                    return;
                }
                String message = resultData.getString(PatternLockActivity.RESULT_KEY);
                if (!message.equals("true")) {
                    Intent intent = new Intent(context, PatternLockActivity.class);
                    intent.putExtra("LockResult", new LockResult(context));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_MULTIPLE_TASK);
                    context.startActivity(intent);
                }

            }

        }
    }

}

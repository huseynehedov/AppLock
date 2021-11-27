package com.huseyn.myapplock.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Process;

public class Utils {
    public static boolean PermissionCheck(Context context){
        AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = opsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(),
                context.getPackageName());

        return mode == AppOpsManager.MODE_ALLOWED;
    }
}

package com.huseyn.myapplock.service;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.util.SparseIntArray;

import com.huseyn.myapplock.listeners.PictureCapturingListener;

public abstract class APictureCapturingService {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }

    private final Activity activity;
    final Context context;
    final CameraManager manager;

    APictureCapturingService(final Activity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    int getOrientation() {
        final int rotation = this.activity.getWindowManager().getDefaultDisplay().getRotation();
        return ORIENTATIONS.get(rotation);
    }

    public abstract void startCapturing(final PictureCapturingListener listener);
}

package com.huseyn.myapplock.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.huseyn.myapplock.R;
import com.huseyn.myapplock.lock.FingerprintActivity;
import com.huseyn.myapplock.lock.PatternLockActivity;
import com.huseyn.myapplock.lock.PinLockActivity;
import com.huseyn.myapplock.service.LockWorker;
import com.huseyn.myapplock.utils.SharedPrefUtil;
import com.huseyn.myapplock.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 2323;
    LinearLayout layoutPermissions;
    private Button buttonSetPassword, buttonChangePassword, buttonAppList;
    private static final String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CODE = 1;
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (!data.getStringExtra(PinLockActivity.RESULT_KEY).equals("true")){
                            requirePassword();
                        }else {
                            doWorker();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !Settings.canDrawOverlays(this)) {
            RequestPermission();
        }
        layoutPermissions = findViewById(R.id.layout_permissions);
        checkPermissions();

        String optionLock = SharedPrefUtil.getInstance(this).getString("LockType");
        if (optionLock != null && !optionLock.isEmpty()){
            requirePassword();
        }else{
            findViewById(R.id.btnAppList).setVisibility(View.GONE);
        }
    }

    public void btnOpenAppList(View view) {
        startActivity(new Intent(MainActivity.this, AppListActivity.class));
    }

    public void btnChangePassword(View view) {
        startActivity(new Intent(MainActivity.this, OptionsActivity.class));
    }

    public void requirePassword(){
        String optionLock = SharedPrefUtil.getInstance(this).getString("LockType");
        if(optionLock.equals("Pin")){
            someActivityResultLauncher.launch(new Intent(MainActivity.this, PinLockActivity.class));
        }else if(optionLock.equals("Pattern")){
            someActivityResultLauncher.launch(new Intent(MainActivity.this, PatternLockActivity.class));
            //startActivity(new Intent(MainActivity.this, PatternLockActivity.class));
        }else if(optionLock.equals("Fingerprint")){
            someActivityResultLauncher.launch(new Intent(MainActivity.this, FingerprintActivity.class));
        }
    }

    private void doWorker(){
        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(LockWorker.class).build();
        WorkManager
                .getInstance(this)
                .enqueue(uploadWorkRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CODE: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        final List<String> neededPermissions = new ArrayList<>();
        for (final String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }
        if (!neededPermissions.isEmpty()) {
            requestPermissions(neededPermissions.toArray(new String[]{}),
                    MY_PERMISSIONS_REQUEST_ACCESS_CODE);
        }
    }

    private void RequestPermission() {
        // Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    public void setPermission(View view) {
        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    // Permission Granted-System will work
                }

            }
        }
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
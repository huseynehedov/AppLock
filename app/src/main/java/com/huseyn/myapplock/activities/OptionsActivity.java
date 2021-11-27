package com.huseyn.myapplock.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.huseyn.myapplock.R;
import com.huseyn.myapplock.lock.FingerprintActivity;
import com.huseyn.myapplock.lock.PatternLockActivity;
import com.huseyn.myapplock.lock.PinLockActivity;
import com.huseyn.myapplock.receiver.ScreenLockReceiver;
import com.huseyn.myapplock.service.LockWorker;
import com.huseyn.myapplock.utils.SharedPrefUtil;

import java.util.ArrayList;

public class OptionsActivity extends AppCompatActivity {
    private Button buttonApply, buttonPassword;
    private Spinner spinnerLockType;
    private ArrayList<String> optionsLockType = new ArrayList<>();
    private ArrayAdapter<String> optionsLockTypeAdapter;
    private String myPassword;
    public static final String USER_PASSWORD_KEY = "USER_PASSWORD_KEY";
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 2323;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !Settings.canDrawOverlays(this)) {
            RequestPermission();
        }

        ScreenLockReceiver br = new ScreenLockReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(br, filter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        myPassword = SharedPrefUtil.getInstance(this).getString(USER_PASSWORD_KEY);
        buttonPassword = findViewById(R.id.buttonPassword);
        buttonApply = findViewById(R.id.buttonApply);
        spinnerLockType = findViewById(R.id.spinnerLockType);

        optionsLockType.add("Pin");
        optionsLockType.add("Pattern");
        optionsLockType.add("Fingerprint");

        optionsLockTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                            android.R.id.text1, optionsLockType);
        spinnerLockType.setAdapter(optionsLockTypeAdapter);
        spinnerLockType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!optionsLockType.get(i).equals("Pin")){
                    buttonPassword.setVisibility(View.GONE);
                }else{
                    buttonPassword.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        if (myPassword.isEmpty() || myPassword.length() != 4){
            buttonPassword.setText("Set Password");
        }else{
            buttonPassword.setText("Update Password");
        }

        doWorker();
    }

    private void doWorker(){
        WorkRequest uploadWorkRequest =
                new OneTimeWorkRequest.Builder(LockWorker.class).build();
        WorkManager
                .getInstance(this)
                .enqueue(uploadWorkRequest);
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

    public void btnApplyClick(View view) {
        String optionLock = optionsLockType.get(spinnerLockType.getSelectedItemPosition());
        if(optionLock == "Pin"){
            startActivity(new Intent(OptionsActivity.this, PinLockActivity.class));
            finish();
        }else if(optionLock.equals("Pattern")){
            startActivity(new Intent(OptionsActivity.this, PatternLockActivity.class));
            finish();
        }else if(optionLock.equals("Fingerprint")){
            startActivity(new Intent(OptionsActivity.this, FingerprintActivity.class));
            finish();
        }
    }

    public void btnPasswordClick(View view) {
        if (myPassword.trim().isEmpty() || myPassword.length() != 4){
            setPassword(this);
        }else{
            buttonUpdate(this);
        }
    }

    private void setPassword(Context con) {
        AlertDialog.Builder alert = new AlertDialog.Builder(con);
        LinearLayout ll = new LinearLayout(con);
        ll.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(con);
        textView.setText("Enter Your Password");
        textView.setTextSize(22f);
        textView.setTextColor(getResources().getColor(R.color.white));
        ll.addView(textView);
        EditText input = new EditText(con);
        input.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Enter Password");
        input.setTextColor(getResources().getColor(R.color.white));
        input.setHintTextColor(Color.WHITE);
        input.setTextSize(16f);
        ll.addView(input);
        ll.setBackgroundColor(Color.BLACK);

        alert.setView(ll);
        alert.setPositiveButton("Okey", (dialogInterface, i) -> {
            SharedPrefUtil.getInstance(OptionsActivity.this).putString(USER_PASSWORD_KEY,
                    input.getText().toString());
            Toast.makeText(con, "Password is set successfully.", Toast.LENGTH_SHORT).show();
            myPassword = input.getText().toString();
            if (!myPassword.trim().isEmpty() && myPassword.length() == 4){
                buttonPassword.setText("Update Password");
            }
        }).setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        alert.show();
    }

    private void buttonUpdate(Context con) {
        AlertDialog.Builder alert = new AlertDialog.Builder(con);
        LinearLayout ll = new LinearLayout(con);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(con);
        textView.setText("Enter previous password");
        textView.setTextSize(22f);
        textView.setTextColor(getResources().getColor(R.color.white));
        ll.addView(textView);

        EditText editText = new EditText(con);
        editText.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editText.setTextColor(getResources().getColor(R.color.white));
        editText.setHintTextColor(getResources().getColor(R.color.white));
        editText.setTextSize(16f);
        editText.setHint("Enter previous Password");
        ll.addView(editText);

        alert.setView(ll);

        TextView textView2 = new TextView(con);
        textView2.setText("Enter new password");
        textView2.setTextSize(22f);
        textView2.setTextColor(getResources().getColor(R.color.white));
        ll.addView(textView2);

        EditText editText2 = new EditText(con);
        editText2.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        editText2.setHint("Enter new Password");
        editText2.setTextColor(getResources().getColor(R.color.white));
        editText2.setHintTextColor(getResources().getColor(R.color.white));
        editText2.setTextSize(16f);
        ll.addView(editText2);

        alert.setView(ll);

        alert.setPositiveButton("Okey", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (editText.getText().toString().equals(myPassword)){
                    SharedPrefUtil.getInstance(OptionsActivity.this).putString(USER_PASSWORD_KEY,
                            editText2.getText().toString());
                    Toast.makeText(con, "Password is update successfully.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(con, "Sorry Old Password is incorrect", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

}
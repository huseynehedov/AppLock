package com.huseyn.myapplock.lock;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huseyn.myapplock.R;
import com.huseyn.myapplock.activities.AppListActivity;
import com.huseyn.myapplock.listeners.PictureCapturingListener;
import com.huseyn.myapplock.service.APictureCapturingService;
import com.huseyn.myapplock.service.LockWorker;
import com.huseyn.myapplock.service.PictureCapturingServiceImpl;

import java.io.File;
import java.util.TreeMap;
import java.util.concurrent.Executor;

public class FingerprintActivity extends AppCompatActivity implements PictureCapturingListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private String resultMessage = "false";
    public static final String RESULT_KEY = "ResultKey";
    private static final int REQUEST_CODE = 100;
    private BiometricManager biometricManager;
    private ImageView imageViewFingerPrint;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private ImageView uploadBackPhoto;
    private ImageView uploadFrontPhoto;
    private APictureCapturingService pictureService;
    Boolean b = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        imageViewFingerPrint = findViewById(R.id.imageView_fingerPrint);
        uploadBackPhoto = (ImageView) findViewById(R.id.backIV);
        uploadFrontPhoto = (ImageView) findViewById(R.id.frontIV);
        pictureService = PictureCapturingServiceImpl.getInstance(FingerprintActivity.this);

        biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG |DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Fingerprint sensor not exits", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:

                Toast.makeText(this, "sensor not avail or busy", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, REQUEST_CODE);
                break;
        }

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(FingerprintActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                System.out.println("Wrong input");
                resultMessage = "false";

                pictureService.startCapturing((PictureCapturingListener) FingerprintActivity.this);

                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();

                b= getIntent().getBooleanExtra("ACTION_SCREEN_ON", false);

                if (b){
                    View decorView = getWindow().getDecorView();
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_IMMERSIVE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
                }else{
                    View decorView = getWindow().getDecorView();
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                System.out.println("Correct input");
                resultMessage = "true";
                finish();
                //startActivity(new Intent(FingerprintActivity.this, AppListActivity.class));
                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                System.out.println("Wrong input");
                resultMessage = "false";

                pictureService.startCapturing((PictureCapturingListener) FingerprintActivity.this);
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        imageViewFingerPrint.setOnClickListener(view -> {
            biometricPrompt.authenticate(promptInfo);
        });
    }

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (picturesTaken != null && !picturesTaken.isEmpty()) {
            return;
        }
    }

    @Override
    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (pictureData != null && pictureUrl != null) {
            runOnUiThread(() -> {
                final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
                final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                if (pictureUrl.contains("0_pic.jpg")) {
                    uploadBackPhoto.setImageBitmap(scaled);
                } else if (pictureUrl.contains("1_pic.jpg")) {
                    uploadFrontPhoto.setImageBitmap(scaled);
                }
            });
        }
    }

    @Override
    public void finish() {

        if (getIntent().getParcelableExtra(LockWorker.KEY_RECEIVER) != null){
            ResultReceiver resultReceiver = getIntent().getParcelableExtra(LockWorker.KEY_RECEIVER);
            Bundle resultData = new Bundle();
            resultData.putString(RESULT_KEY, resultMessage);
            resultReceiver.send(RESULT_OK, resultData);
        }else{
            Intent intent = new Intent();
            intent.putExtra(RESULT_KEY, resultMessage);
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

}
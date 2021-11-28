package com.huseyn.myapplock.lock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.huseyn.myapplock.R;
import com.huseyn.myapplock.activities.AppListActivity;
import com.huseyn.myapplock.listeners.PictureCapturingListener;
import com.huseyn.myapplock.model.Password;
import com.huseyn.myapplock.service.APictureCapturingService;
import com.huseyn.myapplock.service.LockWorker;
import com.huseyn.myapplock.service.PictureCapturingServiceImpl;
import com.shuhart.stepview.StepView;

import java.util.List;
import java.util.TreeMap;

public class PatternLockActivity extends AppCompatActivity implements PictureCapturingListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    StepView stepView;
    LinearLayout linearLayout;
    RelativeLayout relativeLayout;
    Password password;
    String userPassword;
    TextView stateText;

    private String resultMessage = "false";
    public static final String RESULT_KEY = "ResultKey";
    private ImageView uploadBackPhoto;
    private ImageView uploadFrontPhoto;
    private APictureCapturingService pictureService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_lock);

        stepView = findViewById(R.id.stepView);
        linearLayout = findViewById(R.id.LL);
        relativeLayout = findViewById(R.id.main_layout);
        password = new Password(this);
        stateText = findViewById(R.id.textView_state);
        stateText.setText(password.FIRST_USE);

        uploadBackPhoto = (ImageView) findViewById(R.id.backIV);
        uploadFrontPhoto = (ImageView) findViewById(R.id.frontIV);
        pictureService = PictureCapturingServiceImpl.getInstance(PatternLockActivity.this);

        if (password.getPASSWORD_KEY() == null){
            linearLayout.setVisibility(View.GONE);
            stepView.setVisibility(View.VISIBLE);
            stepView.setStepsNumber(2);
            stepView.go(0,true);
        }else{
            linearLayout.setVisibility(View.VISIBLE);
            stepView.setVisibility(View.GONE);
            int backgroundColor = ResourcesCompat.getColor(getResources(), R.color.blue, null);
            relativeLayout.setBackgroundColor(backgroundColor);
            stateText.setTextColor(Color.WHITE);
        }
        setUpPatternListener();
    }

    private void showToast(final String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show()
        );
    }

    private void setUpPatternListener() {
        final PatternLockView patternLockView = findViewById(R.id.patternView);
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                String strPassword = PatternLockUtils.patternToString(patternLockView, pattern);
                if(strPassword.length() < 4){
                    stateText.setText(password.SCHEMA_FAILED);
                    patternLockView.clearPattern();
                    return;
                }
                if (password.getPASSWORD_KEY() == null){
                    if(password.isFist){
                        userPassword = strPassword;
                        password.setFist(false);
                        stateText.setText(password.CONFIRM_PATTERN);
                        stepView.go(1, true);
                    }else{
                        if (userPassword.equals(strPassword)){
                            password.setPASSWORD_KEY(strPassword);
                            stateText.setText(password.PATTERN_SET);
                            stepView.done(true);
                            //doWorker();
                            goToMainActivity();
                        }else{
                            stateText.setText(password.PATTERN_SET);
                        }
                    }
                }else{
                    if (password.isCorrect(strPassword)){
                        //Password True
                        System.out.println("Correct input");
                        resultMessage = "true";
                        finish();

                        stateText.setText(password.PATTERN_SET);
                        //goToMainActivity();
                    }else{
                        //Wrong Password
                        System.out.println("Wrong input");
                        resultMessage = "false";

                        showToast("Starting capture!");
                        pictureService.startCapturing((PictureCapturingListener) PatternLockActivity.this);

                        Toast.makeText(PatternLockActivity.this, "Password wrong", Toast.LENGTH_SHORT).show();
                    }
                }
                patternLockView.clearPattern();
            }

            @Override
            public void onCleared() {

            }
        });
    }

    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (picturesTaken != null && !picturesTaken.isEmpty()) {
            showToast("Done capturing all photos!");
            return;
        }
        showToast("No camera detected!");
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
            showToast("Picture saved to " + pictureUrl);
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


    private void goToMainActivity() {
        Intent i = new Intent(PatternLockActivity.this, AppListActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        if(password.getPASSWORD_KEY() == null && !password.isFist){
            stepView.go(0, true);
            password.setFist(true);
            stateText.setText(password.FIRST_USE);
        }else{
            finish();
            super.onBackPressed();
        }
    }

}
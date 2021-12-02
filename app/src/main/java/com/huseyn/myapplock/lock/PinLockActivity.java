package com.huseyn.myapplock.lock;

import static com.huseyn.myapplock.activities.OptionsActivity.USER_PASSWORD_KEY;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huseyn.myapplock.R;
import com.huseyn.myapplock.listeners.PictureCapturingListener;
import com.huseyn.myapplock.service.APictureCapturingService;
import com.huseyn.myapplock.service.LockWorker;
import com.huseyn.myapplock.service.PictureCapturingServiceImpl;
import com.huseyn.myapplock.utils.SharedPrefUtil;

import java.util.TreeMap;

public class PinLockActivity extends AppCompatActivity  implements PictureCapturingListener,
        ActivityCompat.OnRequestPermissionsResultCallback{
    private StringBuilder secondInput = new StringBuilder();
    private String localPasscode;
    private TextView tv_input_tip, textViewInput;
    private TextView number0, number1, number2, number3, number4, number5,
            number6, number7, number8, number9;
    private TextView[] textViews ;
    private ImageView numberB, numberOK;
    private ImageView iv_lock, iv_ok;

    private String firstInputTip = "Enter a passcode of 4 digits";
    private String wrongLengthTip = "Enter a passcode of 4 digits";
    private String wrongInputTip = "Passcode do not match";
    private String correctInputTip = "Passcode is correct";
    private boolean b = false;

    private int passcodeLength = 4;
    private int correctStatusColor = 0xFF61C560; //0xFFFF0000
    private int wrongStatusColor = 0xFFF24055;
    private int normalStatusColor = 0xFFFFFFFF;
    private int numberTextColor = 0xFF747474;

    private String resultMessage = "false";
    public static final String RESULT_KEY = "ResultKey";
    private ImageView uploadBackPhoto;
    private ImageView uploadFrontPhoto;
    private APictureCapturingService pictureService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        uploadBackPhoto = findViewById(R.id.backIV);
        uploadFrontPhoto = findViewById(R.id.frontIV);
        textViewInput = findViewById(R.id.textViewInput);
        iv_ok = findViewById(R.id.iv_ok);
        tv_input_tip = findViewById(R.id.tv_input_tip);
        tv_input_tip.setText(firstInputTip);
        textViewInput.setTextColor(normalStatusColor);

        number0 = (TextView) findViewById(R.id.number0);
        number1 = (TextView) findViewById(R.id.number1);
        number2 = (TextView) findViewById(R.id.number2);
        number3 = (TextView) findViewById(R.id.number3);
        number4 = (TextView) findViewById(R.id.number4);
        number5 = (TextView) findViewById(R.id.number5);
        number6 = (TextView) findViewById(R.id.number6);
        number7 = (TextView) findViewById(R.id.number7);
        number8 = (TextView) findViewById(R.id.number8);
        number9 = (TextView) findViewById(R.id.number9);
        numberOK = (ImageView) findViewById(R.id.numberOK);
        numberB = (ImageView) findViewById(R.id.numberB);
        textViews = new TextView[]{number0, number1, number2, number3, number4, number5,
                number6, number7, number8, number9};
        pictureService = PictureCapturingServiceImpl.getInstance(PinLockActivity.this);
        localPasscode = SharedPrefUtil.getInstance(this).getString(USER_PASSWORD_KEY);
        iv_ok.setImageResource(R.drawable.lock_24);

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

    public void number0(View view) {
        if (secondInput.length() < 4){
            secondInput.append("0");
            number0.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }
    public void number1(View view) {
        if (secondInput.length() < 4){
            secondInput.append("1");
            number1.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }
    public void number2(View view) {
        if (secondInput.length() < 4){
            secondInput.append("2");
            number2.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }
    public void number3(View view) {
        if (secondInput.length() < 4){
            secondInput.append("3");
            number3.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }
    public void number4(View view) {
        if (secondInput.length() < 4){
            secondInput.append("4");
            number4.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }
    public void number5(View view) {
        if (secondInput.length() < 4){
            secondInput.append("5");
            number5.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }
    public void number6(View view) {
        if (secondInput.length() < 4){
            secondInput.append("6");
            number6.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }
    public void number7(View view) {
        if (secondInput.length() < 4){
            secondInput.append("7");
            number7.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }
    public void number8(View view) {
        if (secondInput.length() < 4){
            secondInput.append("8");
            number8.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }
    public void number9(View view) {
        if (secondInput.length() < 4){
            secondInput.append("9");
            number9.setBackgroundColor(getColor(R.color.light_gray));
            System.out.println(secondInput);
        }
    }

    public void imageOkClikc(View view) {
        System.out.println("ImageOk");
        if (localPasscode.equals(secondInput.toString())){
            tv_input_tip.setText(correctInputTip);
            textViewInput.setTextColor(correctStatusColor);
            System.out.println("Correct input");
            secondInput.delete(0, secondInput.length());
            resultMessage = "true";
            iv_ok.setImageResource(R.drawable.check_24);
            finish();
        }else{
            System.out.println("Wrong input");
            resultMessage = "false";

            pictureService.startCapturing((PictureCapturingListener) PinLockActivity.this);

            Toast.makeText(PinLockActivity.this, "Password wrong", Toast.LENGTH_SHORT).show();
            textViewInput.setTextColor(wrongStatusColor);
            tv_input_tip.setText(wrongInputTip);
            iv_ok.setImageResource(R.drawable.lock_24);
        }
    }

    public void imageBackClick(View view) {
        if (secondInput.length() >= 0 && !secondInput.toString().isEmpty()){
            String findDeletedNumber = String.valueOf(secondInput.charAt(secondInput.length()-1));
            int ind = Integer.valueOf(findDeletedNumber);
            System.out.println("DEl: " + ind);
            secondInput.deleteCharAt(secondInput.length()-1);
           textViews[ind].setBackgroundColor(Color.WHITE);
        }
        if (secondInput.length() == 0){
            secondInput = new StringBuilder();
        }
        System.out.println(secondInput);
        System.out.println(secondInput.length());
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
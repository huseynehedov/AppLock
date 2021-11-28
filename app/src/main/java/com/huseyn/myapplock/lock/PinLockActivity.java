package com.huseyn.myapplock.lock;

import static com.huseyn.myapplock.activities.OptionsActivity.USER_PASSWORD_KEY;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import com.hanks.passcodeview.PasscodeView;
import com.huseyn.myapplock.R;
import com.huseyn.myapplock.listeners.PictureCapturingListener;
import com.huseyn.myapplock.service.APictureCapturingService;
import com.huseyn.myapplock.service.LockWorker;
import com.huseyn.myapplock.service.PictureCapturingServiceImpl;
import com.huseyn.myapplock.utils.SharedPrefUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

public class PinLockActivity extends AppCompatActivity  implements PictureCapturingListener,
        ActivityCompat.OnRequestPermissionsResultCallback{
    private static final int CAPTURE_IMAGE_REQUEST = 1;
    private PasscodeView passcodeView;
    private String resultMessage = "false";
    public static final String RESULT_KEY = "ResultKey";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private File photoFile;
    String currentPhotoPath;
    private ImageView uploadBackPhoto;
    private ImageView uploadFrontPhoto;
    private APictureCapturingService pictureService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        passcodeView = findViewById(R.id.passcodeView);
        uploadBackPhoto = (ImageView) findViewById(R.id.backIV);
        uploadFrontPhoto = (ImageView) findViewById(R.id.frontIV);
        pictureService = PictureCapturingServiceImpl.getInstance(PinLockActivity.this);

        String correctPassword = SharedPrefUtil.getInstance(this).getString(USER_PASSWORD_KEY);
        passcodeView.setPasscodeLength(4).setLocalPasscode(correctPassword);
        passcodeView.setListener(new PasscodeView.PasscodeViewListener() {
           @Override
            public void onFail() {
                System.out.println("Wrong input");
                resultMessage = "false";

                showToast("Starting capture!");
                pictureService.startCapturing((PictureCapturingListener) PinLockActivity.this);

                Toast.makeText(PinLockActivity.this, "Password wrong", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(String number) {
                System.out.println("Correct input");
                resultMessage = "true";
                finish();
            }

        });
    }

    private void showToast(final String text) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show()
        );
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

}
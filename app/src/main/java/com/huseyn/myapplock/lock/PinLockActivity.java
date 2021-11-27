package com.huseyn.myapplock.lock;

import static com.huseyn.myapplock.activities.OptionsActivity.USER_PASSWORD_KEY;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Toast;

import com.hanks.passcodeview.PasscodeView;
import com.huseyn.myapplock.R;
import com.huseyn.myapplock.service.LockWorker;
import com.huseyn.myapplock.utils.SharedPrefUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PinLockActivity extends AppCompatActivity {
    private static final int CAPTURE_IMAGE_REQUEST = 1;
    private PasscodeView passcodeView;
    private String resultMessage = "false";
    public static final String RESULT_KEY = "ResultKey";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private File photoFile;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        passcodeView = findViewById(R.id.passcodeView);

        String correctPassword = SharedPrefUtil.getInstance(this).getString(USER_PASSWORD_KEY);
        passcodeView.setPasscodeLength(4).setLocalPasscode(correctPassword);
        passcodeView.setListener(new PasscodeView.PasscodeViewListener() {

            @Override
            public void onSuccess(String number) {

                resultMessage = "true";
                finish();
            }
            @Override
            public void onFail() {
                resultMessage = "false";
                takePhoto();
                Toast.makeText(PinLockActivity.this, "Password wrong", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void takePhoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI = FileProvider.getUriForFile(
                            this,
                            "com.raywenderlich.testapp.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                }
            } catch (Exception ex) {
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            galleryAddPic();
        }
    }
}
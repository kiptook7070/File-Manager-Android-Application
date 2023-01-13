package com.rbigsoft.sn.unzip.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.unrar.activity.BrowseSdcardActivity;

public class    MainUiActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String CATEGORY_FILE_MAIN = "category_file_main";
    private LinearLayout llImage, llVideo, llDocument, llMusic, llApk, llDownload, llCompress, llAllFolder;
    private Button btnRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ui);
        if (Build.VERSION.SDK_INT >= 23) {
            this.checkPermissions();
        }

        initViews();
    }

    private void checkPermissions() {
        String str = "android.permission.READ_EXTERNAL_STORAGE";
        if (ContextCompat.checkSelfPermission(this, str) != 0 && !ActivityCompat.shouldShowRequestPermissionRationale(this, str)) {
            String str2 = "android.permission.WRITE_EXTERNAL_STORAGE";
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, str2)) {
                ActivityCompat.requestPermissions(this, new String[]{str, str2}, 10);
            }
        }
    }

    private void initViews() {
        btnRate = findViewById(R.id.btn_rate);
        btnRate.setOnClickListener(this);
        llImage = findViewById(R.id.ll_image);
        llImage.setOnClickListener(this);
        llVideo = findViewById(R.id.ll_video);
        llVideo.setOnClickListener(this);
        llDocument = findViewById(R.id.ll_document);
        llDocument.setOnClickListener(this);
        llMusic = findViewById(R.id.ll_music);
        llMusic.setOnClickListener(this);
        llApk = findViewById(R.id.ll_apk);
        llApk.setOnClickListener(this);
        llDownload = findViewById(R.id.ll_download);
        llDownload.setOnClickListener(this);
        llCompress = findViewById(R.id.ll_compress_file);
        llCompress.setOnClickListener(this);
        llAllFolder = findViewById(R.id.ll_all_folder);
        llAllFolder.setOnClickListener(this);
    }

    public void startActivityCategory(String tag) {
        Intent intent = new Intent(this, FileActivity.class);
        intent.putExtra(CATEGORY_FILE_MAIN, tag);
        startActivity(intent);
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 10 && iArr.length > 0) {
            int i2 = iArr[0];
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rate:

                Uri uri = Uri.parse("market://details?id=" + "com.sn.unzip.unzar");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + "com.sn.unzip.unzar")));
                }
                break;
            case R.id.ll_image:
                startActivityCategory("image");
                break;
            case R.id.ll_video:
                startActivityCategory("video");
                break;
            case R.id.ll_document:
                startActivityCategory("document");
                break;
            case R.id.ll_music:
                startActivityCategory("music");
                break;
            case R.id.ll_apk:
                startActivityCategory("apk");
                break;
            case R.id.ll_download:
                startActivityCategory("extracted");
                break;
            case R.id.ll_compress_file:
                startActivityCategory("compress");
                break;
            case R.id.ll_all_folder:
                startActivity(new Intent(MainUiActivity.this, BrowseSdcardActivity.class));
                break;
        }
    }
}

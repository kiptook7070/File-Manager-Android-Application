package com.rbigsoft.sn.unzip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.unrar.activity.BrowseSdcardActivity;

import java.util.Timer;

public class MainLiteVersionActivity extends AppCompatActivity {
    public boolean interstitialCanceled;
    public Timer waitTimer;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.logo);
        setContentView(imageView);
        MainLiteVersionActivity.this.startMainActivity();
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, BrowseSdcardActivity.class);
        intent.setData(getIntent().getData());
        startActivity(intent);
        finish();
    }

    public void onPause() {
        this.waitTimer.cancel();
        this.interstitialCanceled = true;
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        startMainActivity();
    }
}

package com.earnwithkino.kino;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }, 0);
    }
}

package com.earnwithkino.kino;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NoInternetConnection extends BaseActivity implements View.OnClickListener {

    private Button tryAgain;

    public void onConnectivityChanged(boolean isConnected) {
        // Handle connectivity change
//        if (isConnected){
//            startLoginActivity(this);
//        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet_connection);
        tryAgain = findViewById(R.id.button2);
        tryAgain.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == tryAgain) {
           startLoginActivity(this);
        }
    }
}

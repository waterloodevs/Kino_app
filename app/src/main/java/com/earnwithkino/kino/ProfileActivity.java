package com.earnwithkino.kino;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {

    private TextView email;
    private Button logout;
    private ImageView backButton;
    private LinearLayout redeemKin;
    private LinearLayout faq;
    private LinearLayout stores;

    public void onConnectivityChanged(boolean isConnected) {
        // Handle connectivity change
        if (!isConnected){
            startNoInternetConnectionActivity(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
        email = findViewById(R.id.email);
        redeemKin = findViewById(R.id.linearLayout2);
        redeemKin.setOnClickListener(this);
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(this);
        faq = findViewById(R.id.linearLayout3);
        faq.setOnClickListener(this);
        stores = findViewById(R.id.linearLayout4);
        stores.setOnClickListener(this);
        setEmail();
    }

    public void setEmail() {
        FirebaseUser currentUser = getCurrentUser();
        email.setText(currentUser.getEmail());
    }

    private void disableFCM(){
        FirebaseMessaging.getInstance().setAutoInitEnabled(false);
        // Detach fcm token, so user does not get any more notifications.
        // The sever still has the old fcm token but once the user logs in again, it will be
        // overwritten on the server.
        new Thread(() -> {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e){
                e.printStackTrace();
            }
        }).start();
    }

    private void openFaqLink(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.earnwithkino.com/faq"));
        startActivity(browserIntent);
    }

    private void openStoresLink(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.earnwithkino.com/stores"));
        startActivity(browserIntent);
    }

    @Override
    public void onClick(View view) {
        if (view == logout) {
            // Disable FCM auto initialization
            disableFCM();
            // Delete all shared preferences (price and token)
            getSharedPreferences("_", MODE_PRIVATE).edit().clear().apply();
            // Sign out
            signOut();
        } else if (view == backButton){
            startMainActivity(this);
        } else if (view == redeemKin){
            startRedeemActivity(this);
        } else if (view == faq){
            openFaqLink();
        } else if (view == stores){
            openStoresLink();
        }
    }
}



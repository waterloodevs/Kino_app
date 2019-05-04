package com.earnwithkino.kino;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;

import kin.sdk.KinAccount;

import static android.content.Context.MODE_PRIVATE;

public class SettingsActivity extends Fragment implements View.OnClickListener {

    private MainActivity parentActivity;
    private TextView address;
    private Button logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_settings, container, false);
        parentActivity = (MainActivity) getActivity();
        address = fragmentView.findViewById(R.id.textView7);
        logout = fragmentView.findViewById(R.id.button2);
        logout.setOnClickListener(this);
        setAddress();
        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        if (view == logout) {
            // Disable FCM auto initialization
            disableFCM();
            // Delete all shared preferences (price and token)
            parentActivity.getSharedPreferences("_", MODE_PRIVATE).edit().clear().apply();
            // Sign out
            parentActivity.signOut();
        }
    }

    public void setAddress() {
        KinAccount account = parentActivity.account;
        address.setText(account.getPublicAddress());
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

}

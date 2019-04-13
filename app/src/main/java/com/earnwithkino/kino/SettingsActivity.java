package com.earnwithkino.kino;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import kin.sdk.KinAccount;

public class SettingsActivity extends Fragment implements View.OnClickListener {

    private TextView address;
    private Button logout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_settings, container, false);

        address = fragmentView.findViewById(R.id.textView7);
        setAddress();

        logout = fragmentView.findViewById(R.id.button2);
        logout.setOnClickListener(this);

        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        if (view == logout) {
            ((MainActivity) getActivity()).signOut();
        }
    }

    public void setAddress() {
        KinAccount account = ((MainActivity) getActivity()).account;
        address.setText(account.getPublicAddress());
    }
}

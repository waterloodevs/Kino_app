package com.earnwithkino.kino;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class BalanceActivity extends Fragment implements MainActivity.ListenFromActivity, View.OnClickListener {

    private TextView balance;
    private ImageView info;
    private PopupWindow popupWindow;
    private MainActivity parentActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_balance, container, false);
        parentActivity = (MainActivity) getActivity();
        balance = fragmentView.findViewById(R.id.balanceView);
        info = fragmentView.findViewById(R.id.info);
        info.setOnClickListener(this);
        // Since changes to balances are updated in the main activity, set a listener
        parentActivity.setActivityListener(this);
        setKinBalance();
        return fragmentView;
    }

    public void doSomethingInFragment() {
        setKinBalance();
    }

    private void setKinBalance() {
        balance.setText(parentActivity.kinBalance);
    }

    private void showPopup(){
        // Inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater) parentActivity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.earn_popup_window, null);

        // Create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup to dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        // Get a reference for the custom view close button
        ImageButton closeButton = popupView.findViewById(R.id.ib_close);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });

        // Show the popup window
        // Which view you pass in doesn't matter, it is only used for the window token
        popupWindow.showAtLocation(info, Gravity.CENTER, 0, 0);
    }

    public void onClick(View view) {
        if (view == info) {
            showPopup();
        }
    }

}

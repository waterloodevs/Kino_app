package com.earnwithkino.kino;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import kin.sdk.AccountStatus;
import kin.sdk.Balance;
import kin.sdk.Environment;
import kin.sdk.EventListener;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.exception.CreateAccountException;
import kin.utils.ResultCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class WalletActivity extends Fragment implements MainActivity.ListenFromActivity, View.OnClickListener {

    private TextView balance;
    private ImageView info;
    private PopupWindow popupWindow;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_wallet, container, false);
        balance = fragmentView.findViewById(R.id.balanceView);
        info = fragmentView.findViewById(R.id.info);
        info.setOnClickListener(this);
        setKinBalance();
        // Since changes to balances are updated in the main activity, set a listener
        ((MainActivity) getActivity()).setActivityListener(WalletActivity.this);
        return fragmentView;
    }

    public void doSomethingInFragment() {
        setKinBalance();
    }

    public void setKinBalance() {
        balance.setText(((MainActivity) getActivity()).kinBalance);
    }

    public void onClick(View view) {
        if (view == info) {
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.earn_popup_window, null);

            // create the popup window
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

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        }
    }

}

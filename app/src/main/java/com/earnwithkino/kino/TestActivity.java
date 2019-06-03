package com.earnwithkino.kino;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import kin.sdk.Balance;
import kin.sdk.Environment;
import kin.sdk.EventListener;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.exception.OperationFailedException;

public class TestActivity extends BaseActivity implements View.OnClickListener {

    private static final int PRECISION = 0;
    private FirebaseUser currentUser;
    public KinAccount account;
    public String kinBalance = "0";
    private TextView balanceView;
    private ImageView profileButton;
    private Button earnedButton;
    private Button spentButton;

    public void onConnectivityChanged(boolean isConnected) {
        // Handle connectivity change
        if (!isConnected){
            startNoInternetConnectionActivity(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        balanceView = findViewById(R.id.balanceView);
        profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(this);
        earnedButton = findViewById(R.id.earnedButton);
        earnedButton.setOnClickListener(this);
        spentButton = findViewById(R.id.spentButton);
        spentButton.setOnClickListener(this);

        // Before you can deliver the notification on Android 8.0 and higher, you
        // must register your app's notification channel with the system
        createNotificationChannel();

        currentUser = getCurrentUser();
        String uid = currentUser.getUid();
        // Kin client is the manager for Kin accounts
        KinClient kinClient = new KinClient(this, Environment.TEST, STUB_APP_ID, uid);
        // Get account associated with user
        account = kinClient.getAccount(APP_INDEX);
        // Add balance listener
        addBalanceListeners(account);
        // Background tasks
        ExampleAsyncTask task = new ExampleAsyncTask(this);
        task.execute(account);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in, id token exists
        if (currentUser == null){
            startLoginActivity(this);
        }
    }

    private void addBalanceListeners(KinAccount account) {
        account.addBalanceListener(new EventListener<Balance>() {
            @Override
            public void onEvent(Balance balance) {
                // This is fired on the background thread.
                // Moving to the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setKinBalance(balance);
                    }
                });
            }
        });
    }

    private void setKinBalance(Balance balance) {
        kinBalance = balance.value(PRECISION);
        balanceView.setText(kinBalance);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("balance", kinBalance).apply();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Kino";
            String description = "Kino notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Kino_channel_ID", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void handleKinBalanceError(String error){
        Toast.makeText(this, "Unable to fetch Kin balance. " + error, Toast.LENGTH_SHORT).show();
    }

    private static class ExampleAsyncTask extends AsyncTask<KinAccount, String, Void> {

        private Balance balance = null;
        private String error = null;
        private WeakReference<TestActivity> activityWeakReference;
        private ExampleAsyncTask(TestActivity activity){
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(KinAccount... accounts) {
            KinAccount account = accounts[0];
            try{
                balance = account.getBalanceSync();
            } catch (OperationFailedException e){
                try {
                    error = e.getCause().getMessage();
                } catch (Exception ee){
                    error = e.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Get strong reference to the calling activity
            TestActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            if (balance != null){
                activity.setKinBalance(balance);
            } else {
                activity.handleKinBalanceError(error);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == profileButton){
            startProfileActivity(this);
        } else if (view == earnedButton){
            spentButton.setBackgroundResource(R.drawable.spent_btn_rounded);
            earnedButton.setBackgroundResource(R.drawable.active_earned_btn_rounded);
        } else if (view == spentButton){
            earnedButton.setBackgroundResource(R.drawable.earned_btn_rounded);
            spentButton.setBackgroundResource(R.drawable.active_spent_btn_rounded);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

}


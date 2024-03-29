package com.earnwithkino.kino;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import kin.sdk.AccountStatus;
import kin.sdk.Environment;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.OperationFailedException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WalletActivity extends BaseActivity implements View.OnClickListener {

    private KinClient kinClient;
    private Button createWalletButton;
    private FirebaseUser currentUser;
    private boolean walletExistsFlag = false;
    private ImageView backButton;

    public void onConnectivityChanged(boolean isConnected) {
        // Handle connectivity change
        if (!isConnected){
            startNoInternetConnectionActivity(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = getCurrentUser();
        String uid = currentUser.getUid();
        // Kin client is the manager for Kin accounts
        //TODO: check if the kin client is unique across devices, if its not, maybe this can be used to check if the user already has an account on a another device.
        kinClient = new KinClient(this, Environment.TEST, STUB_APP_ID, uid);
        WalletExistsAsyncTask task = new WalletExistsAsyncTask(this);
        task.execute(kinClient);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in, id token exists
        if (currentUser == null){
            startLoginActivity(this);
        }
    }

    private void init(){
        setContentView(R.layout.activity_wallet);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this);
        createWalletButton = findViewById(R.id.createWalletButton);
        createWalletButton.setOnClickListener(this);
        //TODO: if a user signs into another device, their existing wallet on the old device will no longer receive earned kin. Show a warning here. The post request should return a custom code indicating that a public address already existed, continuing will override the old wallet.
        //TODO: if no old address exists then send a transaction for any earned kin so far that hasn't been paid out.
    }

    private void onBoardAccount(KinAccount account) throws IOException{
        String route = "/onboard_account";
        String token;
        try {
            token = getIdToken();
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new IOException();
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("public_address", account.getPublicAddress());
        } catch (JSONException e){
            throw new IOException();
        }
        RequestBody data = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(BASE_URL + route)
                .post(data)
                .build();
        OkHttpClient okHttpClient = getOkHttpClient();
        Response response = okHttpClient.newCall(request).execute();
        final int code = response.code();
        response.close();
        if (code != 201){
            throw new IOException();
        }
    }

    private void handleWalletError(String error){
        Toast.makeText(this, "Wallet creation failed. " + error, Toast.LENGTH_SHORT).show();
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

    public void onClick(View v) {
        if (v == createWalletButton) {
            CreateAndOnboardAsyncTask task = new CreateAndOnboardAsyncTask(this);
            task.execute();
        } else if (v == backButton){
            // Disable FCM auto initialization
            disableFCM();
            // Delete all shared preferences (price and token)
            getSharedPreferences("_", MODE_PRIVATE).edit().clear().apply();
            // Sign out
            signOut();
        }
    }

    private static class WalletExistsAsyncTask extends AsyncTask<KinClient, Void, Void> {

        private WeakReference<WalletActivity> activityWeakReference;
        private WalletExistsAsyncTask(WalletActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Get strong reference to the calling activity
            WalletActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.showProgressDialog();
        }

        @Override
        protected Void doInBackground(KinClient... kinClients) {
            KinClient kinClient = kinClients[0];
            // Get strong reference to the calling activity
            WalletActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return null;
            }
            try {
                if (walletExists(kinClient)){
                    activity.walletExistsFlag = true;
                }
            } catch (OperationFailedException e) {
                //TODO: how to handle if can't check if wallet exists..
                // Maybe a 404 page
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Get strong reference to the calling activity
            WalletActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.hideProgressDialog();
            if (activity.walletExistsFlag){
                activity.startMainActivity(activity);
            } else{
                activity.init();
            }
        }

        private boolean walletExists(KinClient kinClient) throws OperationFailedException {
            boolean status = false;
            if (kinClient.hasAccount()){
                KinAccount account = kinClient.getAccount(APP_INDEX);
                // Check if key pair exists on this device
                if (account != null){
                    // Check if key pair exists on the blockchain
                    int value = account.getStatusSync();
                    if (value == AccountStatus.CREATED) {
                        status = true;
                    }
                }
            }
            return status;
        }
    }

    private static class CreateAndOnboardAsyncTask extends AsyncTask<Void, Void, Void> {

        private String error = null;
        private WeakReference<WalletActivity> activityWeakReference;
        private CreateAndOnboardAsyncTask(WalletActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Get strong reference to the calling activity
            WalletActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Get strong reference to the calling activity
            WalletActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return null;
            }
            try{
                KinAccount account = activity.getKinAccount(activity.kinClient);
                activity.onBoardAccount(account);
                activity.walletExistsFlag = true;
            } catch (CreateAccountException | IOException e){
                try {
                    error = e.getCause().getMessage();
                } catch (Exception ee){
                    error = e.getMessage();
                }
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Get strong reference to the calling activity
            WalletActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.hideProgressDialog();
            if (activity.walletExistsFlag){
                activity.startMainActivity(activity);
            } else{
                activity.handleWalletError(error);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }
}

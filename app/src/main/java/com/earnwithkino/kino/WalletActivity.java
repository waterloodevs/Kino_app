package com.earnwithkino.kino;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.lang.ref.WeakReference;

import kin.sdk.AccountStatus;
import kin.sdk.Environment;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.OperationFailedException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WalletActivity extends BaseActivity implements View.OnClickListener {

    private KinClient kinClient;
    private Button createWalletButton;
    private FirebaseUser currentUser;
    private boolean walletExistsFlag = false;

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
        if ((currentUser == null) || (getIdToken() == null)){
            startLoginActivity(this);
        }
    }

    private void init(){
        setContentView(R.layout.activity_wallet);
        createWalletButton = findViewById(R.id.createWalletButton);
        createWalletButton.setOnClickListener(this);
        //TODO: if a user signs into another device, their existing wallet on the old device will no longer receive earned kin. Show a warning here. The post request should return a custom code indicating that a public address already existed, continuing will override the old wallet.
        //TODO: if no old address exists then send a transaction for any earned kin so far that hasn't been paid out.
    }

    private void onBoardAccount(KinAccount account) throws IOException{
        Request request = new Request.Builder()
                .url(String.format(URL_CREATE_ACCOUNT, account.getPublicAddress()))
                .get()
                .build();
        OkHttpClient okHttpClient = getOkHttpClient();
        Response response = okHttpClient.newCall(request).execute();
        final int code = response.code();
        response.close();
        if (code != 200){
            throw new IOException();
        }
    }

    private void handleWalletError(){
        Toast.makeText(this, "Wallet creation failed. Please try again later.", Toast.LENGTH_SHORT).show();
    }

    public void onClick(View v) {
        if (v == createWalletButton) {
            CreateAndOnboardAsyncTask task = new CreateAndOnboardAsyncTask(this);
            task.execute();
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
                activity.handleWalletError();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }
}

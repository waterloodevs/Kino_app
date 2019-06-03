package com.earnwithkino.kino;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import kin.sdk.AccountStatus;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.exception.CreateAccountException;
import kin.sdk.exception.OperationFailedException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class BaseActivity extends AppCompatActivity implements ConnectivityChangeReceiver.OnConnectivityChangedListener {

    public static final String BASE_URL = "http://3f6c0903.ngrok.io";
    public static final String KINO_PUBLIC_ADDRESS = "GCFEL2AKYJFFNSPDA6CVCD3T3BHD6LZBQLGT5RYP4ZT6WAH42PZ5YNZN6";
    public static final String STUB_APP_ID = "k1n0";
    public static final int APP_INDEX = 0;
    private ProgressDialog mProgressDialog;
    private OkHttpClient okHttpClient;
    private ConnectivityChangeReceiver connectivityChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivityChangeReceiver = new ConnectivityChangeReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityChangeReceiver, filter);
    }

    @Override
    public abstract void onConnectivityChanged(boolean isConnected);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectivityChangeReceiver);
    }

    public FirebaseAuth getFirebaseInstance(){
        return FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser(){
        FirebaseAuth mAuth = getFirebaseInstance();
        return mAuth.getCurrentUser();
    }

    public OkHttpClient getOkHttpClient(){
        return new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    // Make sure to only call this from a background thread
    public void setIdtoken() throws ExecutionException, InterruptedException, TimeoutException{
        //TODO: handle different types of execution exceptions
        FirebaseUser currentUser = getCurrentUser();
        GetTokenResult result = Tasks.await(currentUser.getIdToken(true), 60, TimeUnit.SECONDS);
        String token = result.getToken();
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("token", token).apply();
    }

    public String getIdToken() throws ExecutionException, InterruptedException, TimeoutException{
        FirebaseUser currentUser = getCurrentUser();
        GetTokenResult result = Tasks.await(currentUser.getIdToken(true), 60, TimeUnit.SECONDS);
        return result.getToken();
    }

    public void enableFCM() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
    }

    public void setFCMToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        // Get new Instance ID token
                        InstanceIdResult result = task.getResult();
                        String fcmToken = result.getToken();
                        sendFCMTokenToServer(fcmToken);
                    }
                });
    }

    public void sendFCMTokenToServer(String fcmToken) {

        String route = "/update_fcm_token";
        String token;
        try {
            token = getIdToken();
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            return;
        }

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("android_fcm_token", fcmToken);
        } catch (JSONException e){
            e.printStackTrace();
            return;
        }
        RequestBody data = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(BASE_URL + route)
                .post(data)
                .build();

        okHttpClient = getOkHttpClient();
        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        //TODO: Error handling. "Token post to server call failed"
                    }
                    @Override
                    public void onResponse(Call call, Response response) {
                        final int code = response.code();
                        response.close();
                        if (code != 201) {
                            //TODO: Error handling. "Token post to server failed with response code: "
                        } else {
                            //TODO: Error handling. "Token posted to server successfully"
                        }
                    }
                });

    }

    public void sendUserToServer() throws IOException{
        String route = "/register";
        String token;
        try {
            token = getIdToken();
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new IOException();
        }
        // Send user to backend
        RequestBody data = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("","")
                .build();
        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(BASE_URL + route)
                .post(data)
                .build();
        okHttpClient = getOkHttpClient();
        Response response = okHttpClient.newCall(request).execute();
        final int code = response.code();
        response.close();
        if (code != 201){
            throw new IOException();
        }
    }

//    public void setKinPrice(){
//        String route = "/spend_price_per_dollar";
//        String token = getIdToken();
//        Request request = new Request.Builder()
//                .addHeader("Authorization", "Token " + token)
//                .url(BASE_URL + route)
//                .get()
//                .build();
//        okHttpClient = getOkHttpClient();
//        okHttpClient.newCall(request)
//                .enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        //TODO: Error handling. "Kin price call failed"
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) {
//                        final int code = response.code();
//                        if (code != 200) {
//                            //TODO: Error handling. "Kin Price call failed with response code: "
//                        } else {
//                            try{
//                                String body = response.body().string();
//                                JSONObject data = new JSONObject(body);
//                                int price = Integer.parseInt(data.getString("price"));
//                                //TODO: Error handling. "Kin Price is " + data.getString("price"));
//                                getSharedPreferences("_", MODE_PRIVATE).edit().putInt("price", price).apply();
//                                //TODO: Error handling. "Kin price stored successfully");
//                            } catch (JSONException e){
//                                e.printStackTrace();
//                            } catch (IOException e){
//                                e.printStackTrace();
//                            }
//                        }
//                        response.close();
//                    }
//                });
//    }

    public KinAccount getKinAccount(KinClient kinClient) throws CreateAccountException{
        // The index that is used to get a specific account from the client manager
        KinAccount kinAccount = kinClient.getAccount(APP_INDEX);
        if (kinAccount == null) {
            kinAccount = kinClient.addAccount();
        }
        return kinAccount;
    }

//    public int getKinPrice(){
//        return getSharedPreferences("_", MODE_PRIVATE).getInt("price", 0);
//    }

    public void startLoginActivity(Context context){
        startActivity(new Intent(context, LoginActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((Activity) context).finish();
    }

    public void startSignupActivity(Context context){
        startActivity(new Intent(context, SignupActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((Activity) context).finish();
    }

    public void startMainActivity(Context context){
        startActivity(new Intent(context, TestActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((Activity) context).finish();
    }

    public void startProfileActivity(Context context){
        startActivity(new Intent(context, ProfileActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((Activity) context).finish();
    }

    public void startRedeemActivity(Context context){
        startActivity(new Intent(context, RedeemActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((Activity) context).finish();
    }

    public void startWalletActivity(Context context){
        startActivity(new Intent(context, WalletActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((Activity) context).finish();
    }

    public void startNoInternetConnectionActivity(Context context){
        startActivity(new Intent(context, NoInternetConnection.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        ((Activity) context).finish();
    }

    public void signOut(){
        FirebaseAuth mAuth = getFirebaseInstance();
        mAuth.signOut();
        startLoginActivity(this);
    }
}

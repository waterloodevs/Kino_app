package com.earnwithkino.kino;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import kin.sdk.Environment;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.Transaction;
import kin.sdk.TransactionId;
import kin.sdk.WhitelistableTransaction;
import kin.utils.ResultCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static android.support.constraint.Constraints.TAG;

public class RedeemActivity extends BaseActivity implements View.OnClickListener {

    private int PRICE = 10000;

    private ImageView backButton;
    private EditText emailView;
    private RadioGroup radioGroup;
    private RadioButton option1;
    private RadioButton option2;
    private RadioButton option3;
    private RadioButton selected;
    private TextView quantityView;
    private ImageView plus;
    private ImageView minus;
    private TextView totalView;
    private Button confirm;
    public KinAccount account;
    public KinClient client;
    private FirebaseUser currentUser;

    public void onConnectivityChanged(boolean isConnected) {
        // Handle connectivity change
        if (!isConnected){
            startNoInternetConnectionActivity(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem);

        backButton = findViewById(R.id.backButton);
        emailView = findViewById(R.id.emailView);
        radioGroup = findViewById(R.id.radioGroup);
        option1 = findViewById(R.id.radioButton1);
        option2 = findViewById(R.id.radioButton2);
        option3 = findViewById(R.id.radioButton3);
        selected = null;
        confirm = findViewById(R.id.confirm);
        plus = findViewById(R.id.plus);
        minus = findViewById(R.id.minus);
        quantityView = findViewById(R.id.quantity);
        totalView = findViewById(R.id.total);
        backButton.setOnClickListener(this);
        confirm.setOnClickListener(this);
        plus.setOnClickListener(this);
        minus.setOnClickListener(this);
        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        currentUser = getCurrentUser();
        String uid = currentUser.getUid();
        // Kin client is the manager for Kin accounts
        client = new KinClient(this, Environment.TEST, STUB_APP_ID, uid);
        // Get account associated with user
        account = client.getAccount(APP_INDEX);
    }

    private void handleError(String error){
        hideProgressDialog();
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    private boolean validateForm() {

        // check Email
        String email = emailView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            handleError("Email is required.");
            return false;
        }

        // check amount
        if (selected == null) {
            handleError("Amount is required");
            return false;
        }

        // check quantity
        String quantity = quantityView.getText().toString();
        if (TextUtils.isEmpty(quantity)) {
            handleError("Quantity is required.");
            return false;
        }

        // Check the user has sufficient balance
        String kinBalance = getSharedPreferences("_", MODE_PRIVATE).getString("balance", null);
        if (kinBalance != null){
            int balance = Integer.parseInt(kinBalance);
            int total = Integer.parseInt(totalView.getText().toString());
            if (total > balance){
                handleError("Sorry, you do not have enough Kin.");
                return false;
            } else if (total <= 0) {
                handleError("Something went wrong. Please try again.");
                return false;
            }
        }
        return true;
    }

    private void addToQuantity() {
        int i = Integer.parseInt(quantityView.getText().toString());
        if (i < 10) {
            i = i + 1;
            quantityView.setText(String.valueOf(i));
        }
    }

    private void subtractFromQuantity() {
        int i = Integer.parseInt(quantityView.getText().toString());
        if (i > 1) {
            i = i - 1;
            quantityView.setText(String.valueOf(i));
        }
    }

    private void showSuccesDialog(){
        hideProgressDialog();
        AlertDialog.Builder diolog = new AlertDialog.Builder(this);
        diolog.setTitle("Success");
        diolog.setMessage("Thank you for your purchase. The recipient will receive an email with your e-giftcard within 48 hours.");
        AlertDialog alertDialog = diolog.create();
        alertDialog.show();
    }

    private void executeWhitelistedTransction(String whitelistTransaction){
        kin.utils.Request<TransactionId> sendTransactionRequest = account.sendWhitelistTransaction(whitelistTransaction);
        sendTransactionRequest.run(new ResultCallback<TransactionId>() {
            @Override
            public void onResult(TransactionId result) {
                showSuccesDialog();
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                handleError("Unexpected error. Please try again later.");
            }
        });
    }

    private String sendOrderToServer(WhitelistableTransaction whitelistableTransaction) throws IOException{

        String route = "/buy_giftcard";
        String token;
        try {
            token = getIdToken();
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            throw new IOException();
        }

        // Get order information
        String type = "Amazon";
        String amount = selected.getText().toString().replace("$", "");
        String email = emailView.getText().toString();
        String quantity = quantityView.getText().toString();
        String total = totalView.getText().toString();

        // Send request to backend, returns whitelisted transaction
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject json = new JSONObject();
        try {
            json.put("type", type);
            json.put("amount", amount);
            json.put("email", email);
            json.put("quantity", quantity);
            json.put("total", total);
            json.put("envelope", whitelistableTransaction.getTransactionPayload());
            json.put("network_id", whitelistableTransaction.getNetworkPassphrase());
        } catch (JSONException e){
            throw new IOException();
        }

        RequestBody data = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .addHeader("Authorization", "Token " + token)
                .url(BaseActivity.BASE_URL + route)
                .post(data)
                .build();
        OkHttpClient okHttpClient = getOkHttpClient();
        Response response = okHttpClient.newCall(request).execute();
        final int code = response.code();
        String data_ = response.body().string();
        response.close();
        if (code != 201){
            throw new IOException();
        } else {
            try {
                JSONObject jsonObject = new JSONObject(data_);
                return jsonObject.getString("tx");
            } catch (JSONException e){
                e.printStackTrace();
                throw new IOException();
            }
        }
    }

    private void buyGiftCard(){
        String total = totalView.getText().toString();
        // Send transaction to be whitelisted
        BigDecimal amountInKin = new BigDecimal(total);
        int fee = 1000;
        kin.utils.Request<Transaction> buildTransactionRequest = account.buildTransaction(KINO_PUBLIC_ADDRESS, amountInKin, fee);
        buildTransactionRequest.run(new ResultCallback<Transaction>() {
            @Override
            public void onResult(Transaction result) {
                ExampleAsyncTask backgroundTasks = new ExampleAsyncTask(RedeemActivity.this);
                backgroundTasks.execute(result.getWhitelistableTransaction());
            }
            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                handleError("Unexpected error. Please try again later.");
            }
        });




//        okHttpClient.newCall(request)
//                .enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        String error = null;
//                        try {
//                            error = e.getCause().getMessage();
//                        } catch (Exception ee){
//                            error = e.getMessage();
//                        }
//                        handleError(error);
//                    }
//                    @Override
//                    public void onResponse(Call call, Response response) {
//                        final int code = response.code();
//                        response.close();
//                        if (code != 200) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    handleError("Unexpected error. Please try again later.");
//                                }
//                            });
//                        } else {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    AlertDialog.Builder diolog = new AlertDialog.Builder(getApplicationContext());
//                                    diolog.setTitle("Success");
//                                    diolog.setMessage("Thank you for your purchase. You will receive an email with your e-giftcard within 48 hours.");
//                                    AlertDialog alertDialog = diolog.create();
//                                    alertDialog.show();
//                                    hideProgressDialog();
//                                }
//                            });
//                        }
//                    }
//                });
    }

    private void calcTotal(){
        int amount = 0;
        if (selected != null) {
            amount = Integer.parseInt(selected.getText().toString().replace("$", ""));
        }
        int quantity = Integer.parseInt(quantityView.getText().toString());
        int total = amount * quantity * PRICE;
        totalView.setText(String.valueOf(total));
    }

    private static class ExampleAsyncTask extends AsyncTask<WhitelistableTransaction, String, Void> {

        private boolean success = false;
        private String error = null;
        private String whitelistTransaction = null;
        private WeakReference<RedeemActivity> activityWeakReference;
        private ExampleAsyncTask(RedeemActivity activity){
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(WhitelistableTransaction... whitelistableTransactions) {
            // Get strong reference to the calling activity
            RedeemActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return null;
            }
            WhitelistableTransaction whitelistabletransaction = whitelistableTransactions[0];
            try{
                whitelistTransaction = activity.sendOrderToServer(whitelistabletransaction);
                success = true;
            } catch (IOException e){
                e.printStackTrace();
                error = "Unexpected error. Please try again later.";
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            RedeemActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            if (success){
                activity.executeWhitelistedTransction(whitelistTransaction);
            } else{
                activity.handleError(error);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view == backButton) {
            startProfileActivity(this);
        } else if (view == confirm) {
            showProgressDialog();
            if (validateForm()) {
                buyGiftCard();
            }
        } else if (view == plus) {
            addToQuantity();
            calcTotal();
        } else if (view == minus) {
            subtractFromQuantity();
            calcTotal();
        } else if ((view == option1) || (view == option2) || (view == option3)){
            int selectedId = radioGroup.getCheckedRadioButtonId();
            selected = findViewById(selectedId);
            calcTotal();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }
}


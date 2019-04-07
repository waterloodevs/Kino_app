package com.earnwithkino.kino;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class WalletActivity extends Fragment {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String STUB_APP_ID = "k1n0";
    private static final int APP_INDEX = 0;
    private static final int PRECISION = 0;
    private static final String URL_CREATE_ACCOUNT = "http://friendbot-testnet.kininfrastructure.com?addr=%s&amount=";

    private OkHttpClient okHttpClient;
    private KinClient kinClient;
    private KinAccount account;

    private TextView kinField;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.activity_wallet, container, false);

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();

        kinField = fragmentView.findViewById(R.id.textView6);

        // Kin client is the manager for Kin accounts
        kinClient = new KinClient(getContext(), Environment.TEST, STUB_APP_ID);

        // Kin account is the entity that holds Kins
        // The accounts are stored in the kinClient and accessed with incremental index
        account = getKinAccount(APP_INDEX);

        // Background tasks
        ExampleAsyncTask task = new ExampleAsyncTask();
        task.execute(account);

        return fragmentView;
    }

    private class ExampleAsyncTask extends AsyncTask<KinAccount, String, Balance> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Balance doInBackground(KinAccount... kinAccounts) {

            // If account was just created, submit to blockchain
            // In production send account's public address to backend
            publishProgress("Checking if account exists on blockchain...");
            if (!accountExists(account)) {
                publishProgress("Creating account on blockchain...");
                onBoardAccount(account);
            }

            // Get account balance
            publishProgress("Getting account balance...");
            Balance balance = getKinBalance(account);

            // Listener for balance changes
            publishProgress("Adding balance listeners...");
            addBalanceListeners(account);

            publishProgress("Setting balance to view...");
            return balance;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            Toast.makeText(getContext(), values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Balance balance) {
            super.onPostExecute(balance);
            setKinBalance(balance);
        }

        private boolean accountExists(KinAccount account) {
            try {
                int value = account.getStatusSync();
                if (value == 0) {
                    return false;
                }
            } catch (Exception e) {
                // Error when checking if the account exists on blockchain
                Log.d(TAG, "Error when checking if account exists");
            }
            return true;
        }

        private void onBoardAccount(KinAccount account) {
            // Creating the kin account for a specific user
            if (account != null) {
                Request request = new Request.Builder()
                        .url(String.format(URL_CREATE_ACCOUNT, account.getPublicAddress()))
                        .get()
                        .build();
                okHttpClient.newCall(request)
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d(TAG, "Request call failed");
                            }

                            @Override
                            public void onResponse(Call call, Response response) {
                                final int code = response.code();
                                response.close();
                                if (code != 200) {
                                    Log.d(TAG, "Request call failed with response code: " + code);
                                } else {
                                    Log.d(TAG, "Account created on blockchain");
                                }
                            }
                        });
            }
        }

        private void setKinBalance(Balance balance) {
            String value = balance.value(PRECISION);
            kinField.setText(value);
        }

        private Balance getKinBalance(KinAccount account) {
            Balance balance = new Balance() {
                @Override
                public BigDecimal value() {
                    return BigDecimal.ONE;
                }

                @Override
                public String value(int precision) {
                    return String.valueOf(1);
                }
            };

            try {
                return account.getBalanceSync();
            } catch (Exception e) {
                // Error when looking for account balance
                e.printStackTrace();
                Log.d(TAG, "Error when trying to Kin Balance");
            }

            return balance;
        }

        private void addBalanceListeners(KinAccount account) {
            account.addBalanceListener(new EventListener<Balance>() {
                @Override
                public void onEvent(Balance balance) {
                    setKinBalance(balance);
                }
            });
        }

    }

    public KinAccount getKinAccount(int index) {
        // The index that is used to get a specific account from the client manager
        KinAccount kinAccount = kinClient.getAccount(index);
        try {
            // Creates a local keypair
            if (kinAccount == null) {
                kinAccount = kinClient.addAccount();
                Log.d(TAG, "Created new account succeeded");
            }
        } catch (CreateAccountException e) {
            e.printStackTrace();
            Log.d(TAG, "Unable to create keypair");
        }

        return kinAccount;
    }

}

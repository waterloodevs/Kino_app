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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;

import kin.sdk.Balance;
import kin.sdk.Environment;
import kin.sdk.EventListener;
import kin.sdk.KinAccount;
import kin.sdk.KinClient;
import kin.sdk.exception.OperationFailedException;

public class MainActivity extends BaseActivity {

    private static final int PRECISION = 0;
    private FirebaseUser currentUser;
    public BottomNavigationView nav;
    public KinAccount account;
    public ListenFromActivity activityListener;
    public String kinBalance = "0";

    public interface ListenFromActivity {
        void doSomethingInFragment();
    }

    public void setActivityListener(ListenFromActivity activityListener) {
        this.activityListener = activityListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        nav = findViewById(R.id.navigationView);
        nav.setOnNavigationItemSelectedListener(navListener);

        // Start default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new BalanceActivity())
                .commit();

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
        if ((currentUser == null) || (getIdToken() == null)){
            startLoginActivity(this);
        }
        // TODO: also check somehow if wallet stops existing (on device or on blockchain)
    }

    private void addBalanceListeners(KinAccount account) {
        account.addBalanceListener(new EventListener<Balance>() {
            @Override
            public void onEvent(Balance balance) {
                setKinBalance(balance);
            }
        });
    }

    private void setKinBalance(Balance balance) {
        Toast.makeText(this, "Setting balance to variable...", Toast.LENGTH_SHORT).show();
        kinBalance = balance.value(PRECISION);
        // Only update the balance field in wallet fragment if its open
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof BalanceActivity) {
            activityListener.doSomethingInFragment();
        }
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

    private static class ExampleAsyncTask extends AsyncTask<KinAccount, String, Void> {

        private Balance balance = null;
        private WeakReference<MainActivity> activityWeakReference;
        private ExampleAsyncTask(MainActivity activity){
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
                //TODO: how to handle if couldnt get balance
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // Get strong reference to the calling activity
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            Toast.makeText(activity, values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Get strong reference to the calling activity
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            if (balance != null){
                activity.setKinBalance(balance);
            } else {
                //TODO: how to handle if couldn't get balance
            }
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()){
                        case R.id.navigation_balance:
                            selectedFragment = new BalanceActivity();
                            break;
                        case R.id.navigation_store:
                            selectedFragment = new StoreActivity();
                            break;
                        case R.id.navigation_settings:
                            selectedFragment = new SettingsActivity();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
            };

    public void signOut(){
        FirebaseAuth mAuth = getFirebaseInstance();
        mAuth.signOut();
        startLoginActivity(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

}

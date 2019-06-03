package com.earnwithkino.kino;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button emailSignInButton;
    private TextView emailSignUpButton;

    public void onConnectivityChanged(boolean isConnected) {
        // Handle connectivity change
        if (!isConnected){
            startNoInternetConnectionActivity(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null){
            startWalletActivity(this);
        }
    }

    private void init(){
        // Views
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);
        // Buttons
        emailSignInButton = findViewById(R.id.signInButton);
        emailSignUpButton = findViewById(R.id.signUpButton);
        // Set Listeners
        emailSignInButton.setOnClickListener(this);
        emailSignUpButton.setOnClickListener(this);
    }

    private boolean validForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void signIn() {
        if (!validForm()) {
            return;
        }
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);
        ExampleAsyncTask backgroundTasks = new ExampleAsyncTask(LoginActivity.this);
        backgroundTasks.execute(credentials);
    }

    private void handleSignInError(String error){
        Toast.makeText(this, "Sign In Failed. " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v == emailSignUpButton) {
            startSignupActivity(this);
        } else if (v == emailSignInButton) {
            signIn();
        }
    }

    private static class ExampleAsyncTask extends AsyncTask<Map, String, Void> {

        private boolean success = false;
        private String error = null;
        private WeakReference<LoginActivity> activityWeakReference;
        private ExampleAsyncTask(LoginActivity activity){
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Get strong reference to the calling activity
            LoginActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.showProgressDialog();
        }

        @Override
        protected Void doInBackground(Map... credentials) {
            // Get strong reference to the calling activity
            LoginActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return null;
            }
            String email = credentials[0].get("email").toString();
            String password = credentials[0].get("password").toString();
            try {
                FirebaseAuth mAuth = activity.getFirebaseInstance();
                Tasks.await(mAuth.signInWithEmailAndPassword(email, password), 15, TimeUnit.SECONDS);
//                activity.setIdtoken();
                success = true;
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                try {
                    error = e.getCause().getMessage();
                } catch (Exception ee){
                    error = e.getMessage();
                }
                return null;
            }
            // Send fcm token to server asynchronously
            activity.enableFCM();
            activity.setFCMToken();
            // Get and store Kin price per dollar asynchronously
//            publishProgress("Fetching kin price...");
//            activity.setKinPrice();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // Get strong reference to the calling activity
            LoginActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            Toast.makeText(activity, values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            LoginActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.hideProgressDialog();
            if (success){
                activity.startWalletActivity(activity);
            } else{
                activity.handleSignInError(error);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

}

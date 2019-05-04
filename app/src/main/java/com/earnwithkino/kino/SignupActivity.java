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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class SignupActivity extends BaseActivity implements View.OnClickListener{

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mReenterPasswordField;
    private TextView emailSignInButton;
    private Button emailSignUpButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
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

    public void init(){
        // Views
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);
        mReenterPasswordField = findViewById(R.id.fieldPasswordReenter);
        // Buttons
        emailSignInButton = findViewById(R.id.signInButton1);
        emailSignUpButton = findViewById(R.id.signUpButton1);
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

        String passwordReenter = mReenterPasswordField.getText().toString();
        if (TextUtils.isEmpty(passwordReenter)) {
            mReenterPasswordField.setError("Required.");
            valid = false;
        } else if (!password.equals(passwordReenter)){
            mReenterPasswordField.setError("Passwords don't match.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void createAccount() {
        if (!validForm()) {
            return;
        }
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);
        ExampleAsyncTask backgroundTasks = new ExampleAsyncTask(this);
        backgroundTasks.execute(credentials);
    }

    private void handleSignUpError(){
        Toast.makeText(this, "Sign Up Failed. Please try again.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v == emailSignUpButton) {
            createAccount();
        } else if (v == emailSignInButton) {
            startLoginActivity(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    private static class ExampleAsyncTask extends AsyncTask<Map, String, Void> {

        private boolean success = false;
        private WeakReference<SignupActivity> activityWeakReference;
        private ExampleAsyncTask(SignupActivity activity){
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Get strong reference to the calling activity
            SignupActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.showProgressDialog();
        }

        @Override
        protected Void doInBackground(Map... credentials) {
            // Get strong reference to the calling activity
            SignupActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return null;
            }
            String email = credentials[0].get("email").toString();
            String password = credentials[0].get("password").toString();
            try {
                FirebaseAuth mAuth = activity.getFirebaseInstance();
                Tasks.await(mAuth.createUserWithEmailAndPassword(email, password), 15, TimeUnit.SECONDS);
                activity.setIdtoken();
                activity.sendUserToServer();
                success = true;
            } catch (ExecutionException | TimeoutException | InterruptedException | IOException e) {
                //TODO: handle different types of execution exceptions
                Throwable ee = e.getCause ();
                if (!(ee instanceof FirebaseAuthUserCollisionException)){
                    // delete the user in case it was signed up and setidtoken or sendusertoserver failed
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        try{
                            Tasks.await(user.delete(), 20, TimeUnit.SECONDS);
                        } catch (ExecutionException | TimeoutException | InterruptedException eee) {
                            //TODO: handle different types of execution exceptions
                            //TODO: how to handle if can't delete user
                        }
                    }
                }
                return null;
            }
            // Send fcm token to server asynchronously
            publishProgress("Sending fcm token to server...");
            activity.enableFCM();
            activity.setFCMToken();
            // Get and store Kin price per dollar asynchronously
            publishProgress("fetching kin price...");
            activity.setKinPrice();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // Get strong reference to the calling activity
            SignupActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            Toast.makeText(activity, values[0], Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Get strong reference to the calling activity
            SignupActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()){
                return;
            }
            activity.hideProgressDialog();
            if (success){
                activity.startWalletActivity(activity);
            } else{
                activity.handleSignUpError();
            }
        }
    }
}

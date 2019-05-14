package com.earnwithkino.kino;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Button;

import java.io.IOException;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    private OnConnectivityChangedListener listener;

    public ConnectivityChangeReceiver(OnConnectivityChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        boolean isNetwork = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
        boolean isInternet;
        try {
            isInternet = isInternetAvailable();
        } catch (InterruptedException | IOException e){
            isInternet = false;
        }
        boolean isConnected = (isNetwork & isInternet);
        listener.onConnectivityChanged(isConnected);
    }

    public interface OnConnectivityChangedListener {
        void onConnectivityChanged(boolean isConnected);
    }

    public boolean isInternetAvailable() throws InterruptedException, IOException {
        //TODO: change this to the server address
        final String command = "ping -i 5 -c 1 google.com";
        return Runtime.getRuntime().exec(command).waitFor() == 0;
    }
}

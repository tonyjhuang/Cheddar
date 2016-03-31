package com.tonyjhuang.cheddar.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.SystemService;

import rx.subjects.BehaviorSubject;

/**
 * Created by tonyjhuang on 3/30/16.
 */
@EReceiver
public class ConnectivityBroadcastReceiver extends BroadcastReceiver {

    public static BehaviorSubject<Status> connectionObservable = BehaviorSubject.create();

    @SystemService
    ConnectivityManager connectivityManager;

    /**
     * Forces a refresh of the current connection status.
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Status networkStatus = getConnectionStatus(cm);
        if(networkStatus != connectionObservable.getValue()) {
            connectionObservable.onNext(networkStatus);
        }
        return networkStatus == Status.CONNECTED;
    }

    /**
     * Get the last known network connection state.
     */
    public static boolean getLastKnownConnected() {
        return connectionObservable.getValue() == Status.CONNECTED;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Status networkStatus = getConnectionStatus(connectivityManager);
        if(networkStatus != connectionObservable.getValue()) {
            connectionObservable.onNext(networkStatus);
        }
    }

    private static Status getConnectionStatus(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting()) {
            return Status.CONNECTED;
        } else {
            return Status.DISCONNECTED;
        }
    }

    public enum Status {CONNECTED, DISCONNECTED}
}

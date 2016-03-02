package com.tonyjhuang.cheddar.service;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by tonyjhuang on 3/1/16.
 */
public class CheddarGcmListenerService extends GcmListenerService {

    private static final String TAG = CheddarGcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.e(TAG, ">>>>>>>>>>>>>>>>>PUSHHHHHH<<<<<<<<<<<<<<<<<<<");
        Log.e(TAG, "from: " + from);
        Log.e(TAG, "data: " + data.toString());
    }
}

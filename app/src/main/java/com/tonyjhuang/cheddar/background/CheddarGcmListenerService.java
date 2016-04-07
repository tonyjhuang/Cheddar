package com.tonyjhuang.cheddar.background;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by tonyjhuang on 3/1/16.
 */
public class CheddarGcmListenerService extends GcmListenerService {

    private static final String TAG = CheddarGcmListenerService.class.getSimpleName();
    public static final String MESSAGE_EVENT_ACTION = "com.tonyjhuang.cheddar.MESSAGE_EVENT";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Timber.e(">>>>>>>>>>>>>>>>>PUSHHHHHH<<<<<<<<<<<<<<<<<<<");
        String payload = data.getString("payload");
        if (payload != null) {
            try {
                JSONObject json = new JSONObject(payload);
                Intent intent = new Intent(MESSAGE_EVENT_ACTION);
                intent.putExtra("payload", payload);
                sendOrderedBroadcast(intent, null);
            } catch (JSONException e) {
                Log.e(TAG, "Couldn't parse payload into json: " + payload);
            }
        } else {
            Log.e(TAG, "couldn't parse gcm message: " + data.toString());
        }
    }
}

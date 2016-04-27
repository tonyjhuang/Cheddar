package com.tonyjhuang.cheddar.background.notif;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tonyjhuang.cheddar.api.network.ResultUnwrapperTypeAdapterFactory;
import com.tonyjhuang.cheddar.background.notif.payload.GcmChatEventPayload;
import com.tonyjhuang.cheddar.background.notif.payload.GcmPayload;
import com.tonyjhuang.cheddar.background.notif.payload.GcmPayloadDeserializer;

import timber.log.Timber;

/**
 * Created by tonyjhuang on 3/1/16.
 */
public class CheddarGcmListenerService extends GcmListenerService {

    public static final String MESSAGE_EVENT_ACTION = "com.tonyjhuang.cheddar.MESSAGE_EVENT";
    private static final String TAG = CheddarGcmListenerService.class.getSimpleName();
    private static final Gson gson = createGson();


    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new ResultUnwrapperTypeAdapterFactory())
                .registerTypeAdapter(GcmPayload.class, new GcmPayloadDeserializer())
                .create();
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Timber.i(">>>>>>>>>>>>>>>>>PUSHHHHHH<<<<<<<<<<<<<<<<<<<");
        String payloadString = data.getString("payload");
        if (payloadString != null) {
            GcmPayload payload = gson.fromJson(payloadString, GcmPayload.class);
            Timber.d("payload: " + payload.toString());
            if (payload instanceof GcmChatEventPayload) {
                Timber.d("sending broadcast with chatevent: " + ((GcmChatEventPayload) payload).chatEvent);
                Intent intent = new Intent(MESSAGE_EVENT_ACTION);
                intent.putExtra("chatEvent", ((GcmChatEventPayload) payload).chatEvent);
                sendOrderedBroadcast(intent, null);
            } else {
                Timber.w("Unknown incoming gcm payload: " + payloadString);
            }
        } else {
            Timber.e("missing payload: " + data.toString());
        }
    }
}

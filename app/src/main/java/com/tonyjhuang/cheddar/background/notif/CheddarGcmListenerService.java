package com.tonyjhuang.cheddar.background.notif;

import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.network.ResultUnwrapperTypeAdapterFactory;
import com.tonyjhuang.cheddar.background.notif.payload.GcmChatEventPayload;
import com.tonyjhuang.cheddar.background.notif.payload.GcmPayload;
import com.tonyjhuang.cheddar.background.notif.payload.GcmPayloadDeserializer;

import timber.log.Timber;

/**
 * Receives GCM messages, extracts ChatEvents from them and rebroadcasts them
 * to the app.
 */
public class CheddarGcmListenerService extends GcmListenerService {

    public static final String CHAT_EVENT_ACTION = "com.tonyjhuang.cheddar.CHAT_EVENT";
    private static final Gson gson = createGson();


    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(new ResultUnwrapperTypeAdapterFactory())
                .registerTypeAdapter(GcmPayload.class, new GcmPayloadDeserializer())
                .registerTypeAdapter(ChatEvent.ChatEventType.class, ChatEvent.ChatEventType.DESERIALIZER)
                .create();
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String payloadString = data.getString("payload");
        if (payloadString != null) {
            GcmPayload payload = gson.fromJson(payloadString, GcmPayload.class);
            if (payload instanceof GcmChatEventPayload) {
                Intent intent = new Intent(CHAT_EVENT_ACTION);
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

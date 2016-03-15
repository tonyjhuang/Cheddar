package com.tonyjhuang.cheddar.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tonyjhuang.cheddar.api.CheddarParser;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.MessageEvent;
import com.tonyjhuang.cheddar.api.models.Presence;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Handles Gcm payloads if no Activity wants to handle it.
 */
@EReceiver
public class GcmBroadcastReceiver extends BroadcastReceiver {

    public static final String MESSAGE_NOTIF_DELETED_ACTION = "com.tonyjhuang.cheddar.MESSAGE_NOTIF_DELETED";
    private static final String TAG = GcmBroadcastReceiver.class.getSimpleName();
    @Bean
    CheddarNotificationService notificationService;
    @Bean
    UnreadMessagesCounter unreadMessagesCounter;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive!");
        String payloadString = intent.getStringExtra("payload");
        try {
            MessageEvent messageEvent = CheddarParser.parseMessageEvent(new JSONObject(payloadString));
            switch (messageEvent.getType()) {
                case PRESENCE:
                    notificationService.createOrUpdatePresenceNotification(context, (Presence) messageEvent);
                    unreadMessagesCounter.increment(((Presence) messageEvent).getAlias().getChatRoomId());
                    break;
                case MESSAGE:
                    notificationService.createOrUpdateMessageNotification(context, (Message) messageEvent);
                    unreadMessagesCounter.increment(((Message) messageEvent).getAlias().getChatRoomId());
                    break;
                default:
                    Log.e(TAG, "Unrecognized MessageEvent: " + payloadString);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse payload into json: " + payloadString);
        } catch (CheddarParser.UnrecognizedParseException e) {
            Log.e(TAG, "Failed to parse json into MessageEvent: " + payloadString);
        }
    }
}

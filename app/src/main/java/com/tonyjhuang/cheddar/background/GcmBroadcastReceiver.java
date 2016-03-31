package com.tonyjhuang.cheddar.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarParser;
import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.api.models.Message;
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

    private static final String TAG = GcmBroadcastReceiver.class.getSimpleName();
    @Bean
    CheddarNotificationService notificationService;
    @Bean
    UnreadMessagesCounter unreadMessagesCounter;
    @Bean
    CheddarApi api;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive!");
        String payloadString = intent.getStringExtra("payload");
        try {
            ChatEvent chatEvent = CheddarParser.parseChatEvent(new JSONObject(payloadString));
            api.getCurrentUser().subscribe(currentUser -> {
                if(!currentUser.getObjectId().equals(chatEvent.getAlias().getUserId())) {
                    handleMessageEvent(context, chatEvent);
                }
            }, error -> Log.e(TAG, "Couldnt fetch current user. "));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse payload into json: " + payloadString);
        } catch (CheddarParser.UnrecognizedParseException e) {
            Log.e(TAG, "Failed to parse json into ChatEvent: " + payloadString);
        }
    }

    private void handleMessageEvent(Context context, ChatEvent chatEvent) {
        switch (chatEvent.getType()) {
            case PRESENCE:
                notificationService.createOrUpdatePresenceNotification(context, (Presence) chatEvent);
                unreadMessagesCounter.increment(chatEvent.getAlias().getChatRoomId());
                break;
            case MESSAGE:
                notificationService.createOrUpdateMessageNotification(context, (Message) chatEvent);
                unreadMessagesCounter.increment(chatEvent.getAlias().getChatRoomId());
                break;
        }
    }
}

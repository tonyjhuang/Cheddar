package com.tonyjhuang.cheddar.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarParser;
import com.tonyjhuang.cheddar.api.models.parse.ParseChatEvent;

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
            ParseChatEvent parseChatEvent = CheddarParser.parseChatEvent(new JSONObject(payloadString));
            api.getCurrentUser().subscribe(currentUser -> {
                if (!currentUser.getObjectId().equals(parseChatEvent.getAlias().getUserId())) {
                    handleChatEvent(context, parseChatEvent);
                }
            }, error -> Log.e(TAG, "Couldnt fetch current user. "));
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse payload into json: " + payloadString);
        } catch (CheddarParser.UnparseableException e) {
            Log.e(TAG, "Failed to parse json into ParseChatEvent: " + payloadString);
        }
    }

    private void handleChatEvent(Context context, ParseChatEvent parseChatEvent) {
        notificationService.createOrUpdateChatEventNotification(context, parseChatEvent);
        unreadMessagesCounter.increment(parseChatEvent.getAlias().getChatRoomId());
    }
}

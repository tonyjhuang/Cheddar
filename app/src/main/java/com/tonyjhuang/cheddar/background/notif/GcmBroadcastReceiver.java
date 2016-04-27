package com.tonyjhuang.cheddar.background.notif;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.background.UnreadMessagesCounter;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;

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

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive!");
        if(intent.hasExtra("chatEvent")) {
            ChatEvent chatEvent = intent.getParcelableExtra("chatEvent");
            handleChatEvent(context, chatEvent);
        }
    }

    private void handleChatEvent(Context context, ChatEvent parseChatEvent) {
        notificationService.createOrUpdateChatEventNotification(context, parseChatEvent);
        unreadMessagesCounter.increment(parseChatEvent.alias().chatRoomId());
    }
}

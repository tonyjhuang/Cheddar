package com.tonyjhuang.cheddar.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

/**
 * For use in an Activity, aborts ordered broadcasts that
 */
public class ChatPushBroadcastReceiver extends BroadcastReceiver {

    private final String chatRoomId;

    public ChatPushBroadcastReceiver(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra("chatEvent")) {
            ChatEvent chatEvent = intent.getParcelableExtra("chatEvent");
            if(chatEvent.alias().chatRoomId().equals(chatRoomId)) {
                abortBroadcast();
            }
        }
    }
}

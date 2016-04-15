package com.tonyjhuang.cheddar.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tonyjhuang.cheddar.api.CheddarParser;
import com.tonyjhuang.cheddar.api.models.ChatEvent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * For use in an Activity, aborts ordered broadcasts that
 */
public class ChatPushBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = ChatPushBroadcastReceiver.class.getSimpleName();
    private final String chatRoomId;

    public ChatPushBroadcastReceiver(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "activity on receive!");
        try {
            ChatEvent chatEvent = CheddarParser.parseChatEvent(
                    new JSONObject(intent.getStringExtra("payload")));
            if (chatEvent.getChatRoomId().equals(chatRoomId)) {
                Log.d(TAG, "aborting push broadcast.");
                abortBroadcast();
            }

        } catch (JSONException | CheddarParser.UnparseableException e) {
            Log.e(TAG, "couldn't parse gcm payload into ChatEvent: " + intent.getStringExtra("payload"));
            abortBroadcast();
        }
    }
}

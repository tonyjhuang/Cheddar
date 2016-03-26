package com.tonyjhuang.cheddar.ui.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tonyjhuang.cheddar.api.CheddarParser;
import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;

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
            switch (chatEvent.getType()) {
                case MESSAGE:
                    handleMessage((Message) chatEvent);
                    break;
                case PRESENCE:
                    handlePresence((Presence) chatEvent);
                    break;
            }
        } catch (JSONException | CheddarParser.UnrecognizedParseException e) {
            Log.e(TAG, "couldn't parse gcm payload into ChatEvent: " + intent.getStringExtra("payload"));
            abortBroadcast();
        }
    }

    private void handlePresence(Presence presence) {
        if (presence.getAlias().getChatRoomId().equals(chatRoomId)) {
            Log.d(TAG, "message event matches current chatroom id");
            abortBroadcast();
        }
    }

    private void handleMessage(Message message) {
        if (message.getAlias().getChatRoomId().equals(chatRoomId)) {
            Log.d(TAG, "message event matches current chatroom id");
            abortBroadcast();
        }
    }
}

package com.tonyjhuang.cheddar.api.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Container for all information required to display a ChatRoom in a list.
 */
public class ChatRoomInfo {
    public ChatRoom chatRoom;
    public Alias alias;
    /**
     * The  most recent ChatEvent sent to chatRoom.
     */
    public ChatEvent chatEvent;

    public ChatRoomInfo(ChatRoom chatRoom, Alias alias, ChatEvent chatEvent) {
        this.chatRoom = chatRoom;
        this.alias = alias;
        this.chatEvent = chatEvent;
    }

    public static ChatRoomInfo fromJson(JSONObject object) throws JSONException {
        ChatRoom chatRoom = ChatRoom.fromJson(object.getJSONObject("chatRoom"));
        Alias alias = Alias.fromJson(object.getJSONObject("alias"));
        ChatEvent chatEvent = ChatEvent.fromJson(object.getJSONObject("chatEvent"));
        return new ChatRoomInfo(chatRoom, alias, chatEvent);
    }

    @Override
    public String toString() {
        return "ChatRoomInfo{" +
                "chatRoom=" + chatRoom +
                ", alias=" + alias +
                ", chatEvent=" + chatEvent +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoomInfo that = (ChatRoomInfo) o;
        return chatRoom.getObjectId().equals(that.chatRoom.getObjectId());
    }

    @Override
    public int hashCode() {
        return chatRoom.getObjectId().hashCode();
    }
}

package com.tonyjhuang.cheddar.api.models.value;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

/**
 * Container for all information required to display a ChatRoom in a list,
 * including the most recently sent ChatEvent for that ChatRoom.
 */
@AutoValue
public abstract class ChatRoomInfo {

    public static ChatRoomInfo create(ChatRoom chatRoom, Alias alias, ChatEvent chatEvent) {
        return new AutoValue_ChatRoomInfo(chatRoom, alias, chatEvent);
    }

    public static TypeAdapter<ChatRoomInfo> typeAdapter(Gson gson) {
        return new AutoValue_ChatRoomInfo.GsonTypeAdapter(gson);
    }

    public abstract ChatRoom chatRoom();

    public abstract Alias alias();

    public abstract ChatEvent chatEvent();
}

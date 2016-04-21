package com.tonyjhuang.cheddar.api.models.value;

import com.google.auto.value.AutoValue;

/**
 * Container for all information required to display a ChatRoom in a list,
 * including the most recently sent ChatEvent for that ChatRoom.
 */
@AutoValue
public abstract class ChatRoomInfo {

    public static ChatRoomInfo create(ChatRoom chatRoom, Alias alias, ChatEvent chatEvent) {
        return new AutoValue_ChatRoomInfo(chatRoom, alias, chatEvent);
    }


    public abstract ChatRoom chatRoom();

    public abstract Alias alias();

    public abstract ChatEvent chatEvent();
}

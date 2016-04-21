package com.tonyjhuang.cheddar.api.models;


import com.tonyjhuang.cheddar.api.models.parse.ParseAlias;
import com.tonyjhuang.cheddar.api.models.parse.ParseChatEvent;
import com.tonyjhuang.cheddar.api.models.parse.ParseChatRoom;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoom;
import com.tonyjhuang.cheddar.api.models.value.MetaData;

/**
 * Created by tonyjhuang on 4/16/16.
 */
public class ParseToValueTranslator {

    public static Alias toAlias(ParseAlias alias) {
        MetaData metaData = MetaData.create(alias.getObjectId(),
                alias.getCreatedAt(),
                alias.getUpdatedAt());

        return Alias.create(metaData,
                alias.getName(),
                alias.isActive(),
                alias.getChatRoomId(),
                alias.getUserId());
    }

    public static ChatEvent toChatEvent(ParseChatEvent chatEvent) {
        MetaData metaData = MetaData.create(chatEvent.getObjectId(),
                chatEvent.getCreatedAt(),
                chatEvent.getUpdatedAt());

        return ChatEvent.create(metaData,
                toType(chatEvent.getType()),
                toAlias(chatEvent.getAlias()),
                chatEvent.getBody());
    }

    public static ChatEvent.ChatEventType toType(ParseChatEvent.Type type) {
        return ChatEvent.ChatEventType.fromString(type.toString());
    }

    public static ChatRoom toChatRoom(ParseChatRoom chatRoom) {
        MetaData metaData = MetaData.create(chatRoom.getObjectId(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt());

        return ChatRoom.create(metaData,
                chatRoom.getMaxOccupancy(),
                chatRoom.getNumOccupants());
    }
}

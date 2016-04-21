package com.tonyjhuang.cheddar.ui.chat.chatevent;

import com.tonyjhuang.cheddar.api.models.parse.ParseChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

import java.util.Date;

/**
 * Holder for MessageEvents for our Chat Adapter.
 * Contains the ParseChatEvent and some metadata around it.
 * Has either a Message OR a Presence.
 */
public class ChatEventViewInfo {

    public ChatEvent chatEvent;
    public Direction direction;
    public Status status;

    /**
     * Used for holding Message ChatEvents.
     */
    public ChatEventViewInfo(ChatEvent chatEvent, Direction direction, Status status) {
        this.chatEvent = chatEvent;
        this.direction = direction;
        this.status = status;
    }

    /**
     * Used for holding Presence ChatEvents that don't have a Direction or Status.
     */
    public ChatEventViewInfo(ChatEvent chatEvent) {
        this(chatEvent, null, null);
    }

    public Date getDate() {
        return chatEvent.metaData().createdAt();
    }

    public boolean hasSameAuthor(ChatEventViewInfo otherInfo) {
        return chatEvent.alias().metaData().objectId().equals(otherInfo.chatEvent.alias().metaData().objectId());
    }

    public boolean isSameObject(ChatEventViewInfo otherInfo) {
        return chatEvent.equals(otherInfo.chatEvent);
    }

    public enum Status {
        SENDING, SENT, FAILED
    }

    public enum Direction {
        INCOMING, OUTGOING
    }
}

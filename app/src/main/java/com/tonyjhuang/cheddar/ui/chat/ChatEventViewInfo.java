package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;

import java.util.Date;

/**
 * Holder for MessageEvents for our Chat Adapter.
 * Contains the ChatEvent and some metadata around it.
 * Has either a Message OR a Presence.
 */
public abstract class ChatEventViewInfo {

    public Direction direction;
    public Status status;

    public ChatEventViewInfo(Direction direction, Status status) {
        this.direction = direction;
        this.status = status;
    }

    public abstract Date getDate();

    public abstract boolean hasSameAuthor(ChatEventViewInfo otherInfo);

    public abstract Message getMessage();

    public abstract Presence getPresence();

    public abstract boolean isSameObject(ChatEventViewInfo otherInfo);

    public enum Status {
        SENDING, SENT, FAILED
    }

    public enum Direction {
        INCOMING, OUTGOING
    }
}

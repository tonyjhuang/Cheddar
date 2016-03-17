package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;

import java.util.Date;

/**
 * Holder for MessageEvents for our Chat Adapter.
 * Contains the MessageEvent and some metadata around it.
 * Has either a Message OR a Presence.
 */
public abstract class ChatItemViewInfo {

    public Direction direction;
    public Status status;

    public ChatItemViewInfo(Direction direction, Status status) {
        this.direction = direction;
        this.status = status;
    }

    public abstract Date getDate();

    public abstract boolean hasSameAuthor(ChatItemViewInfo otherInfo);

    public abstract Message getMessage();

    public abstract Presence getPresence();


    public enum Status {
        SENDING, SENT, FAILED
    }

    public enum Direction {
        INCOMING, OUTGOING
    }
}

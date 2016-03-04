package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;

import java.util.Date;

/**
 * Holder for MessageEvents for our Chat Adapter.
 * Contains the MessageEvent and some metadata around it.
 * Has either a Message OR a Presence.
 */
public class ChatItemViewInfo {

    public Message message;
    public Direction direction;
    public Status status;

    public Presence presence;

    public ChatItemViewInfo(Message message, Direction direction, Status status) {
        this.message = message;
        this.direction = direction;
        this.status = status;
    }

    public ChatItemViewInfo(Presence presence) {
        this.presence = presence;
    }

    public Date getDate() {
        return hasMessage() ? message.getCreatedAt() :
                presence.action == Presence.Action.JOIN ? presence.alias.getCreatedAt()
                        : presence.alias.getUpdatedAt();
    }

    public boolean hasSameAuthor(ChatItemViewInfo otherInfo) {
        return otherInfo.hasMessage() &&
                message.getAlias().getUserId().equals(
                        otherInfo.message.getAlias().getUserId());
    }

    public boolean hasMessage() {
        return message != null;
    }

    public enum Status {
        SENDING, SENT, FAILED
    }

    public enum Direction {
        INCOMING, OUTGOING
    }

    @Override
    public String toString() {
        return direction + "," + status + "," +
                (hasMessage() ? message.toString() : presence.toString());
    }
}

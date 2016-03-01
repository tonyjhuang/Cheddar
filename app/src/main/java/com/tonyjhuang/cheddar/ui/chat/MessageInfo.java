package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.Message;

/**
 * Created by tonyjhuang on 2/27/16.
 */
public class MessageInfo {

    public Message message;
    public Direction direction;
    public Status status;

    public MessageInfo(Message message, Direction direction, Status status) {
        this.message = message;
        this.direction = direction;
        this.status = status;
    }

    public boolean hasSameAuthor(MessageInfo otherInfo) {
        return message.getAlias().getUserId().equals(otherInfo.message.getAlias().getUserId());
    }


    public enum Status {
        SENDING, SENT, FAILED
    }

    public enum Direction {
        INCOMING, OUTGOING
    }
}

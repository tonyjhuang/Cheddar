package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;

import java.util.Date;

/**
 * Created by tonyjhuang on 3/17/16.
 */

public class MessageChatEventViewInfo extends ChatEventViewInfo {

    public Message message;

    public MessageChatEventViewInfo(Message message, Direction direction, Status status) {
        super(direction, status);
        this.message = message;
    }

    public Date getDate() {
        return message.getCreatedAt();
    }

    public boolean hasSameAuthor(ChatEventViewInfo otherInfo) {
        Message otherMessage = otherInfo.getMessage();
        return otherMessage != null &&
                message.getAlias().getUserId().equals(otherMessage.getAlias().getUserId());
    }

    @Override
    public boolean isSameObject(ChatEventViewInfo otherInfo) {
        Message otherMessage = otherInfo.getMessage();
        return otherMessage != null &&
                message.getObjectId().equals(otherMessage.getObjectId());
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public Presence getPresence() {
        return null;
    }

    @Override
    public String toString() {
        return "message - (" + direction + "|" + status + ") " +
                message.getAlias().getName() + " - " + message.getBody();
    }
}

package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;

import java.util.Date;

/**
 * Created by tonyjhuang on 3/17/16.
 */

public class MessageChatItemViewInfo extends ChatItemViewInfo {

    public Message message;

    public MessageChatItemViewInfo(Message message, Direction direction, Status status) {
        super(direction, status);
        this.message = message;
    }

    public Date getDate() {
        return message.getCreatedAt();
    }

    public boolean hasSameAuthor(ChatItemViewInfo otherInfo) {
        Message otherMessage = otherInfo.getMessage();
        return otherMessage != null &&
                message.getAlias().getUserId().equals(otherMessage.getAlias().getUserId());
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

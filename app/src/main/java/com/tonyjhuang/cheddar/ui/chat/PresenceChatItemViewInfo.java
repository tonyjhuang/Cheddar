package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;

import java.util.Date;

/**
 * Created by tonyjhuang on 3/17/16.
 */

public class PresenceChatItemViewInfo extends ChatItemViewInfo {

    public Presence presence;

    public PresenceChatItemViewInfo(Presence presence) {
        super(null, null);
        this.presence = presence;
    }

    public Date getDate() {
        return presence.getAction() == Presence.Action.JOIN
                ? presence.getAlias().getCreatedAt()
                : presence.getAlias().getUpdatedAt();
    }

    public boolean hasSameAuthor(ChatItemViewInfo otherInfo) {
        return false;
    }

    @Override
    public Message getMessage() {
        return null;
    }

    @Override
    public Presence getPresence() {
        return presence;
    }

    @Override
    public String toString() {
        return "presence - " + presence.getAlias().getName() + " : " + presence.getAction();
    }
}


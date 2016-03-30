package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.Presence;

import java.util.Date;

/**
 * Created by tonyjhuang on 3/17/16.
 */

public class PresenceChatEventViewInfo extends ChatEventViewInfo {

    public Presence presence;

    public PresenceChatEventViewInfo(Presence presence) {
        super(null, null);
        this.presence = presence;
    }

    public Date getDate() {
        return presence.getAction() == Presence.Action.JOIN
                ? presence.getAlias().getCreatedAt()
                : presence.getAlias().getUpdatedAt();
    }

    public boolean hasSameAuthor(ChatEventViewInfo otherInfo) {
        return false;
    }

    @Override
    public boolean isSameObject(ChatEventViewInfo otherInfo) {
        Presence otherPresence = otherInfo.getPresence();
        return otherPresence != null && presence.getAction().equals(otherPresence.getAction())
                && presence.getAlias().getObjectId().equals(otherPresence.getAlias().getObjectId());
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


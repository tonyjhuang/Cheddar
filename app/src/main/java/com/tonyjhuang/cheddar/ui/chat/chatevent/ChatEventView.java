package com.tonyjhuang.cheddar.ui.chat.chatevent;

/**
 * Interface for views that display ChatEvents.
 */
public interface ChatEventView {
    void setChatEventViewInfos(ChatEventViewInfo info, ChatEventViewInfo prev, ChatEventViewInfo next);
}

package com.tonyjhuang.cheddar.api.network.response.replaychatevent;

import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

import java.util.Date;
import java.util.List;

/**
 * Created by tonyjhuang on 4/22/16.
 */
public class ChatEventPage {

    public Date startTimeToken;
    public Date endTimeToken;
    public List<ChatEvent> chatEvents;

    public ChatEventPage(Date startTimeToken, Date endTimeToken, List<ChatEvent> chatEvents) {
        this.startTimeToken = startTimeToken;
        this.endTimeToken = endTimeToken;
        this.chatEvents = chatEvents;
    }
}

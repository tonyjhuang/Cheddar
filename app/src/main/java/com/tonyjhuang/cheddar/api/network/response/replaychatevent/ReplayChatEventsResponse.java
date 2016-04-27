package com.tonyjhuang.cheddar.api.network.response.replaychatevent;

import com.google.gson.annotations.SerializedName;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjhuang on 4/22/16.
 */
public class ReplayChatEventsResponse {
    public String startTimeToken;
    public String endTimeToken;
    @SerializedName("events")
    public List<ReplayChatEventObjectHolder> objects;

    public ReplayChatEventsResponse(String startTimeToken, String endTimeToken, List<ReplayChatEventObjectHolder> objects) {
        this.startTimeToken = startTimeToken;
        this.endTimeToken = endTimeToken;
        this.objects = objects;
    }

    public List<ChatEvent> getChatEvents() {
        List<ChatEvent> chatEvents = new ArrayList<>();
        for (ReplayChatEventObjectHolder o : objects) {
            if (o instanceof ReplayChatEventChatEventHolder) {
                chatEvents.add(((ReplayChatEventChatEventHolder) o).chatEvent);
            }
        }
        return chatEvents;
    }
}

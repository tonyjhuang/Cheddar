package com.tonyjhuang.cheddar.background.notif.payload;

import com.google.gson.annotations.SerializedName;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

public class GcmChatEventPayload extends GcmPayload {
    @SerializedName("object")
    public ChatEvent chatEvent;

    @Override
    public String toString() {
        return "GcmChatEventPayload{" +
                "chatEvent=" + chatEvent +
                '}';
    }
}

package com.tonyjhuang.cheddar.background.notif.payload;

import com.google.gson.annotations.SerializedName;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

/**
 * Created by tonyjhuang on 4/27/16.
 */
public class GcmChatEventPayload extends GcmPayload {
    @SerializedName("object")
    public ChatEvent chatEvent;
}

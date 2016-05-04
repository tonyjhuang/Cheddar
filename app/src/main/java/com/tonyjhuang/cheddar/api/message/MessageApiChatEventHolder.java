package com.tonyjhuang.cheddar.api.message;

import com.google.gson.annotations.SerializedName;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

/**
 * Container for a ChatEvent that's sent through our MessageApi.
 */
public class MessageApiChatEventHolder extends MessageApiObjectHolder{
    @SerializedName("object")
    public ChatEvent chatEvent;
}

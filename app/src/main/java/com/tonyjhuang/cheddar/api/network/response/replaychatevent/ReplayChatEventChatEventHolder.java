package com.tonyjhuang.cheddar.api.network.response.replaychatevent;

import com.google.gson.annotations.SerializedName;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

/**
 * Created by tonyjhuang on 4/22/16.
 */
public class ReplayChatEventChatEventHolder extends ReplayChatEventObjectHolder {
    @SerializedName("object")
    ChatEvent chatEvent;
}

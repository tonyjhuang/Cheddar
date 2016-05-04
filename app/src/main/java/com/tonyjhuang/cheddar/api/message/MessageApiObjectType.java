package com.tonyjhuang.cheddar.api.message;

import com.google.gson.annotations.SerializedName;

/**
 * The type of objects that can be sent through our MessageApi.
 */
public enum MessageApiObjectType {
    @SerializedName("ChatEvent")
    CHATEVENT
}

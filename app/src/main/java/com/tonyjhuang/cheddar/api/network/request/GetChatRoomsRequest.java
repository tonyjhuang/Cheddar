package com.tonyjhuang.cheddar.api.network.request;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GetChatRoomsRequest {

    public static GetChatRoomsRequest create(String userId) {
        return new AutoValue_GetChatRoomsRequest(userId);
    }

    public abstract String userId();
}

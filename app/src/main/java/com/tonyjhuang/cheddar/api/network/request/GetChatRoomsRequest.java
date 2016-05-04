package com.tonyjhuang.cheddar.api.network.request;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue
public abstract class GetChatRoomsRequest {

    public static GetChatRoomsRequest create(String userId) {
        return new AutoValue_GetChatRoomsRequest(userId);
    }

    public static TypeAdapter<GetChatRoomsRequest> typeAdapter(Gson gson) {
        return new AutoValue_GetChatRoomsRequest.GsonTypeAdapter(gson);
    }

    public abstract String userId();
}

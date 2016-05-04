package com.tonyjhuang.cheddar.api.network.response.replaychatevent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * ReplayChatEvents api hook might return objects that are not ChatEvents. This will
 * deserialize them into their proper representations.
 */
public class ReplayChatEventDeserializer implements JsonDeserializer<ReplayChatEventObjectHolder> {
    @Override
    public ReplayChatEventObjectHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if(!jsonObject.has("objectType")) return new ReplayChatEventUnknownHolder();

        ReplayChatEventObjectType objectType =
                context.deserialize(jsonObject.get("objectType"), ReplayChatEventObjectType.class);
        if(objectType == null) {
            return new ReplayChatEventUnknownHolder();
        }
        switch(objectType) {
            case CHATEVENT:
                return context.deserialize(json, ReplayChatEventChatEventHolder.class);
            default:
                return new ReplayChatEventUnknownHolder();
        }
    }
}
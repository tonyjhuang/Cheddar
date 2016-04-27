package com.tonyjhuang.cheddar.background.notif.payload;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Deserializes incoming GCM push notifications into their respective GcmPayload representations.
 */
public class GcmPayloadDeserializer implements JsonDeserializer<GcmPayload> {
    @Override
    public GcmPayload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (!jsonObject.has("objectType")) return new GcmUnknownPayload();

        GcmPayloadType objectType =
                context.deserialize(jsonObject.get("objectType"), GcmPayloadType.class);
        switch (objectType) {
            case CHATEVENT:
                return context.deserialize(json, GcmChatEventPayload.class);
            default:
                return new GcmUnknownPayload();
        }
    }
}

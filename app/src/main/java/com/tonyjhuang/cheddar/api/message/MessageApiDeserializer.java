package com.tonyjhuang.cheddar.api.message;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Deserializes objects sent through our MessageApi based on
 * their object type.
 */
public class MessageApiDeserializer implements JsonDeserializer<MessageApiObjectHolder>{
    @Override
    public MessageApiObjectHolder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if(!jsonObject.has("objectType")) return new MessageApiUnknownHolder();

        MessageApiObjectType objectType =
                context.deserialize(jsonObject.get("objectType"), MessageApiObjectType.class);
        if(objectType == null) {
            return new MessageApiUnknownHolder();
        }
        switch(objectType) {
            case CHATEVENT:
                return context.deserialize(json, MessageApiChatEventHolder.class);
            default:
                return new MessageApiUnknownHolder();
        }
    }
}

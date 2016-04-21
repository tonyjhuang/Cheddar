package com.tonyjhuang.cheddar.api.simplepersist;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.Map;

@AutoValue
public abstract class UnreadMessages {

    public static UnreadMessages create(Map<String, Integer> messages) {
        return new AutoValue_UnreadMessages(messages);
    }

    public static TypeAdapter<UnreadMessages> typeAdapter(Gson gson) {
        return new AutoValue_UnreadMessages.GsonTypeAdapter(gson);
    }

    public abstract Map<String, Integer> messages();
}

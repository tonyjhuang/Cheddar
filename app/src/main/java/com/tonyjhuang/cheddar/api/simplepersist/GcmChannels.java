package com.tonyjhuang.cheddar.api.simplepersist;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.Set;

@AutoValue
public abstract class GcmChannels {

    public static GcmChannels create(Set<String> channels) {
        return new AutoValue_GcmChannels(channels);
    }

    public static TypeAdapter<GcmChannels> typeAdapter(Gson gson) {
        return new AutoValue_GcmChannels.GsonTypeAdapter(gson);
    }

    public abstract Set<String> channels();
}

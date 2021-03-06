package com.tonyjhuang.cheddar.api.models.value;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Date;

/**
 * Maps a User to a ChatRoom and contains additional information
 * this relationship (i.e. the name to represent the user with)
 */
@AutoValue
public abstract class Alias implements Parcelable{

    public static Alias create(MetaData metaData,
                               String name,
                               boolean active,
                               String chatRoomId,
                               String userId,
                               int colorId) {
        return new AutoValue_Alias(metaData.objectId(),
                metaData.createdAt(),
                metaData.updatedAt(),
                name, active, chatRoomId, userId, colorId);
    }

    public static TypeAdapter<Alias> typeAdapter(Gson gson) {
        return new AutoValue_Alias.GsonTypeAdapter(gson);
    }

    public abstract String objectId();

    public abstract Date createdAt();

    public abstract Date updatedAt();

    public abstract String name();

    public String displayName() {
        return WordUtils.capitalizeFully(name());
    }

    public abstract boolean active();

    public abstract Alias withActive(boolean active);

    public abstract String chatRoomId();

    public abstract String userId();

    public abstract int colorId();

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Alias that = (Alias) o;
        return objectId().equals(that.objectId());
    }

    public int hashCode() {
        return objectId().hashCode();
    }
}

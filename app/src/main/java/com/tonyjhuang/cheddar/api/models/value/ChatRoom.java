package com.tonyjhuang.cheddar.api.models.value;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.Date;

/**
 * A 'room' that Users can join using Aliases, has
 * a number of associated ChatEvents as well.
 */
@AutoValue
public abstract class ChatRoom {

    public static ChatRoom create(MetaData metaData,
                                  int maxOccupancy,
                                  int numOccupants,
                                  String name) {
        return new AutoValue_ChatRoom(metaData.objectId(),
                metaData.createdAt(),
                metaData.updatedAt(),
                maxOccupancy, numOccupants, name);
    }

    public static TypeAdapter<ChatRoom> typeAdapter(Gson gson) {
        return new AutoValue_ChatRoom.GsonTypeAdapter(gson);
    }

    public abstract String objectId();

    public abstract Date createdAt();

    public abstract Date updatedAt();

    public abstract int maxOccupancy();

    public abstract int numOccupants();

    @Nullable
    public abstract String name();

    public String displayName() {
        return name() == null ? "Group" : name();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoom that = (ChatRoom) o;
        return objectId().equals(that.objectId());
    }

    public int hashCode() {
        return objectId().hashCode();
    }
}

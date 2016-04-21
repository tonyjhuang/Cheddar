package com.tonyjhuang.cheddar.api.models.value;

import com.google.auto.value.AutoValue;

/**
 * A 'room' that Users can join using Aliases, has
 * a number of associated ChatEvents as well.
 */
@AutoValue
public abstract class ChatRoom {

    public static ChatRoom create(MetaData metaData,
                                  int maxOccupancy,
                                  int numOccupants) {
        return new AutoValue_ChatRoom(metaData, maxOccupancy, numOccupants);
    }

    public abstract MetaData metaData();

    public abstract int maxOccupancy();

    public abstract int numOccupants();

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoom that = (ChatRoom) o;
        return metaData().equals(that.metaData());
    }

    public int hashCode() {
        return metaData().hashCode();
    }
}

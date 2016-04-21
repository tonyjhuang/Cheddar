package com.tonyjhuang.cheddar.api.models.value;

import com.google.auto.value.AutoValue;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Maps a User to a ParseChatRoom and contains additional information
 * this relationship (i.e. the name to represent the user with)
 */
@AutoValue
public abstract class Alias {

    public static Alias create(MetaData metaData,
                               String name,
                               boolean active,
                               String chatRoomId,
                               String userId) {
        return new AutoValue_Alias(metaData, name, active, chatRoomId, userId);
    }

    public abstract MetaData metaData();

    public abstract String name();

    public String displayName() {
        return WordUtils.capitalizeFully(name());
    }

    public abstract boolean active();

    public abstract String chatRoomId();

    public abstract String userId();

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Alias that = (Alias) o;
        return metaData().equals(that.metaData());
    }

    public int hashCode() {
        return metaData().hashCode();
    }
}

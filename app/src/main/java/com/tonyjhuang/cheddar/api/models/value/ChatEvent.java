package com.tonyjhuang.cheddar.api.models.value;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * A single event that can be sent to through a ChatRoom,
 * originates from a single Alias.
 */
@AutoValue
public abstract class ChatEvent implements Parcelable {

    /**
     * Create a local (unpersisted) Message, should be replaced
     * by whatever Message is returned by the network.
     */
    public static ChatEvent createPlaceholderMessage(Alias alias,
                                                     String body) {
        String objectId = UUID.randomUUID().toString();
        Date now = new Date();
        MetaData metaData = MetaData.create(objectId, now, now);
        return create(metaData, ChatEventType.MESSAGE, alias, body);
    }


    public static ChatEvent create(MetaData metaData,
                                   ChatEventType type,
                                   Alias alias,
                                   String body) {
        return new AutoValue_ChatEvent(metaData.objectId(),
                metaData.createdAt(),
                metaData.updatedAt(),
                type,
                alias,
                body);
    }

    public static TypeAdapter<ChatEvent> typeAdapter(Gson gson) {
        return new AutoValue_ChatEvent.GsonTypeAdapter(gson);
    }

    public String displayBody() {
        switch (type()) {
            case MESSAGE:
                return alias().displayName() + ": " + body();
            case PRESENCE:
                return body();
            default:
                return "";
        }
    }

    public abstract String objectId();

    public abstract Date createdAt();

    public abstract Date updatedAt();

    public abstract ChatEventType type();

    public abstract Alias alias();

    public abstract String body();

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatEvent that = (ChatEvent) o;
        return objectId().equals(that.objectId());
    }

    public int hashCode() {
        return objectId().hashCode();
    }

    public enum ChatEventType {
        MESSAGE, PRESENCE;

        public static ChatEventType fromString(String string) {
            return ChatEventType.valueOf(string.toUpperCase(Locale.US));
        }
    }
}

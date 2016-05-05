package com.tonyjhuang.cheddar.api.models.value;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

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
        return create(metaData, objectId, ChatEventType.MESSAGE, alias, body);
    }


    public static ChatEvent create(MetaData metaData,
                                   String messageId,
                                   ChatEventType type,
                                   Alias alias,
                                   String body) {
        return new AutoValue_ChatEvent(metaData.objectId(),
                metaData.createdAt(),
                metaData.updatedAt(),
                messageId,
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
                return alias().displayName() + " send a message.";
        }
    }

    public abstract String objectId();

    public abstract Date createdAt();

    public abstract Date updatedAt();

    @Nullable
    public abstract String messageId();

    public abstract ChatEventType type();

    public abstract Alias alias();

    public abstract String body();

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatEvent that = (ChatEvent) o;
        if(type() == ChatEventType.MESSAGE) {
            String messageId = messageId();
            return messageId != null && messageId.equals(that.messageId());
        } else {
            return objectId().equals(that.objectId());
        }
    }

    public int hashCode() {
        return objectId().hashCode();
    }

    public enum ChatEventType {
        MESSAGE, PRESENCE, UNKNOWN;

        public static final JsonDeserializer<ChatEventType> DESERIALIZER = new JsonDeserializer<ChatEventType>() {
            @Override
            public ChatEventType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                try {
                    return fromString(json.getAsString());
                } catch (Exception e) {
                    Timber.e("failed to deserialize " + json.getAsString() + " as ChatEventType.");
                    return UNKNOWN;
                }
            }
        };

        public static ChatEventType fromString(String string) {
            return ChatEventType.valueOf(string.toUpperCase(Locale.US));
        }
    }
}

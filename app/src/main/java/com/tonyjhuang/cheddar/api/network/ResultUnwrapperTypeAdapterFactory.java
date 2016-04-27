package com.tonyjhuang.cheddar.api.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoom;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.value.User;

import java.io.IOException;

/**
 * Unwraps network responses which are typically shaped like {"result": ...}
 */
public class ResultUnwrapperTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {

        Class<? super T> rawType = type.getRawType();
        if (rawType.equals(ChatRoomInfo.class)) {
            return (TypeAdapter<T>) new TypeAdapterWrapper<>(ChatRoomInfo.typeAdapter(gson), gson);
        } else if (rawType.equals(Alias.class)) {
            return (TypeAdapter<T>) new TypeAdapterWrapper<>(Alias.typeAdapter(gson), gson);
        } else if (rawType.equals(ChatEvent.class)) {
            return (TypeAdapter<T>) new TypeAdapterWrapper<>(ChatEvent.typeAdapter(gson), gson);
        } else if (rawType.equals(ChatRoom.class)) {
            return (TypeAdapter<T>) new TypeAdapterWrapper<>(ChatRoom.typeAdapter(gson), gson);
        } else if (rawType.equals(User.class)) {
            return (TypeAdapter<T>) new TypeAdapterWrapper<>(User.typeAdapter(gson), gson);
        }

        // Default
        return new TypeAdapterWrapper<>(gson.getDelegateAdapter(this, type), gson).nullSafe();
    }

    private static class TypeAdapterWrapper<T> extends TypeAdapter<T> {
        final Gson gson;
        final TypeAdapter<T> delegate;
        final TypeAdapter<JsonElement> elementAdapter;

        public TypeAdapterWrapper(TypeAdapter<T> delegate, Gson gson) {
            this.delegate = delegate;
            this.gson = gson;
            elementAdapter = gson.getAdapter(JsonElement.class);
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            delegate.write(out, value);
        }

        @Override
        public T read(JsonReader in) throws IOException {
            JsonElement jsonElement = elementAdapter.read(in);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("result")) {
                    jsonElement = jsonObject.get("result");
                }
            }
            return delegate.fromJsonTree(jsonElement);
        }
    }
}
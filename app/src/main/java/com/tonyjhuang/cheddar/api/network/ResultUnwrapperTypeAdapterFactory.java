package com.tonyjhuang.cheddar.api.network;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Method;

import timber.log.Timber;

public class ResultUnwrapperTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {

        Class<? super T> rawType = type.getRawType();
        try {
            // Call static typeAdapter method on class if it exists, otherwise
            // return the default type adapter.
            Method typeAdapterMethod = rawType.getDeclaredMethod("typeAdapter", Gson.class);
            TypeAdapter<T> typeAdapter = (TypeAdapter<T>) typeAdapterMethod.invoke(null, gson);
            return new TypeAdapterWrapper<>(typeAdapter, gson);
        } catch (Exception e) {
            Timber.e("couldn't invoke static typeAdapter method on " + rawType + ": " + e);
            return new TypeAdapterWrapper<>(gson.getDelegateAdapter(this, type), gson).nullSafe();
        }
    }

    /**
     * Unwraps network responses which are typically shaped like {"result": ...}
     */
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
package com.tonyjhuang.cheddar.api.models.value;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Method;

import timber.log.Timber;

/**
 * Created by tonyjhuang on 4/27/16.
 */
public class ValueTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, final TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        Timber.d("rawType: " + rawType);
        try {
            if(rawType.equals(AutoValue_User.class)) {
                return (TypeAdapter<T>) AutoValue_User.typeAdapter(gson);
            }
            // Call static typeAdapter method on class if it exists, otherwise
            // return the default type adapter.
            Method typeAdapterMethod = rawType.getDeclaredMethod("typeAdapter", Gson.class);
            return (TypeAdapter<T>) typeAdapterMethod.invoke(null, gson);
        } catch (Exception e) {
            Timber.v("couldn't invoke static typeAdapter method on " + rawType + ": " + e);
            return gson.getDelegateAdapter(this, type).nullSafe();
        }
    }
}

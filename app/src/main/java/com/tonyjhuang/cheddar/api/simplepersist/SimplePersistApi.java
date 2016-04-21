package com.tonyjhuang.cheddar.api.simplepersist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.tonyjhuang.cheddar.CheddarPrefs_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by tonyjhuang on 4/20/16.
 */
@EBean
public class SimplePersistApi {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new AutoValueTypeAdapterFactory())
            .create();

    @Pref
    CheddarPrefs_ prefs;

    public void persist(UnreadMessages unreadMessages) {
        prefs.unreadMessages().put(gson.toJson(unreadMessages));
    }

    public void persist(GcmChannels gcmChannels) {
        prefs.pushChannels().put(gson.toJson(gcmChannels));
    }

    public UnreadMessages fetchUnreadMessages() {
        return gson.fromJson(prefs.unreadMessages().get(), UnreadMessages.class);
    }

    public GcmChannels fetchGcmChannels() {
        return gson.fromJson(prefs.pushChannels().get(), GcmChannels.class);
    }

    private static class AutoValueTypeAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            Class<? super T> rawType = type.getRawType();
            if (rawType.equals(UnreadMessages.class)) {
                return (TypeAdapter<T>) UnreadMessages.typeAdapter(gson);
            } else if (rawType.equals(GcmChannels.class)) {
                return (TypeAdapter<T>) GcmChannels.typeAdapter(gson);
            }
            return null;
        }
    }
}

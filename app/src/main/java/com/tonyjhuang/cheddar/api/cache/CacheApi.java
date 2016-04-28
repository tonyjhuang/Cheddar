package com.tonyjhuang.cheddar.api.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.tonyjhuang.cheddar.api.models.realm.RealmChatEvent;
import com.tonyjhuang.cheddar.api.models.realm.RealmUser;
import com.tonyjhuang.cheddar.api.models.realm.ValueSource;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.api.models.value.ValueTypeAdapterFactory;

import org.androidannotations.annotations.EBean;

import java.util.Date;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Action1;

/**
 * Simple on-device persistent store for our Value objects.
 */
@EBean(scope = EBean.Scope.Singleton)
public class CacheApi {

    /**
     * Serializes Dates into Longs. Why? Realm can only deserialize Dates
     * from timestamps in millis.
     */
    private final JsonSerializer<Date> dateJsonSerializer =
            (src, typeOfSrc, context) -> new JsonPrimitive(src.getTime());

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new ValueTypeAdapterFactory())
            .registerTypeAdapter(Date.class, dateJsonSerializer)
            .create();

    /****************
     * User
     ****************/

    public Observable<User> getUser(String userId) {
        return Observable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            return Observable.just(realm.copyFromRealm(realm.where(RealmUser.class)
                    .equalTo("objectId", userId)
                    .findFirst()))
                    .compose(toValue(User.class));
        });
    }

    public Observable<User> persist(User user) {
        return toJson(user)
                .doOnNext(saveToDisk(RealmUser.class))
                .map(string -> user);
    }

    /****************
     * ChatEvent
     ****************/

    public Observable<ChatEvent> persist(ChatEvent chatEvent) {
        return toJson(chatEvent)
                .doOnNext(saveToDisk(RealmChatEvent.class))
                .map(string -> chatEvent);
    }

    /****************
     * Misc
     ****************/

    /**
     * Returns an Action1 that saves a JSON string to Realm.
     */
    private Action1<String> saveToDisk(Class clazz) {
        return json -> {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.createOrUpdateObjectFromJson(clazz, json);
            realm.commitTransaction();
        };
    }

    /**
     * Turns an object into its JSON string representation.
     */
    private <T> Observable<String> toJson(T object) {
        return Observable.just(gson.toJson(object));
    }

    /**
     * Transforms an observable that emits objects that CAN BE CAST AS A ValueSource
     * and changes them to objects of type |targetClass|.
     */
    private <S, T> Observable.Transformer<S, ? extends T> toValue(Class<T> targetClass) {
        return o -> o.cast(ValueSource.class)
                .map(ValueSource::toValue)
                .cast(targetClass);
    }

}

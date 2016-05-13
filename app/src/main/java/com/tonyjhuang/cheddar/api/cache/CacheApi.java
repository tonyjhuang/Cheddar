package com.tonyjhuang.cheddar.api.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.tonyjhuang.cheddar.api.models.realm.RealmAlias;
import com.tonyjhuang.cheddar.api.models.realm.RealmChatEvent;
import com.tonyjhuang.cheddar.api.models.realm.RealmChatRoom;
import com.tonyjhuang.cheddar.api.models.realm.RealmUser;
import com.tonyjhuang.cheddar.api.models.realm.ValueSource;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoom;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.api.models.value.ValueTypeAdapterFactory;

import org.androidannotations.annotations.EBean;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.Sort;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import timber.log.Timber;

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

    /**
     * Maps from Realm to Value types.
     */
    private HashMap<Class, Class> realmToValue = new HashMap<>();

    public CacheApi() {
        realmToValue.put(RealmAlias.class, Alias.class);
        realmToValue.put(RealmChatEvent.class, ChatEvent.class);
        realmToValue.put(RealmChatRoom.class, ChatRoom.class);
        realmToValue.put(RealmUser.class, User.class);
    }

    /****************
     * ! DEBUG !
     ****************/

    public Observable<Void> debugReset() {
        return Observable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.deleteAll();
            realm.commitTransaction();
            realm.close();
            return Observable.just(null);
        });
    }


    /****************
     * Alias
     ****************/

    public Observable<Alias> getAliasForChatRoom(String userId, String chatRoomId) {
        return Observable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            return Observable.just(realm.copyFromRealm(realm.where(RealmAlias.class)
                    .equalTo("userId", userId)
                    .equalTo("chatRoomId", chatRoomId)
                    .findFirst()))
                    .doAfterTerminate(realm::close)
                    .map(RealmAlias::toValue);
        });
    }

    /**
     * Retrieve all active Aliases for the User with the given id.
     */
    public Observable<List<Alias>> getActiveAliasesForUser(String userId) {
        return Observable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            return Observable.just(realm.copyFromRealm(
                    realm.where(RealmAlias.class)
                            .equalTo("userId", userId)
                            .equalTo("active", true)
                            .findAll()))
                    .flatMap(Observable::from)
                    .map(RealmAlias::toValue)
                    .toList()
                    .doOnTerminate(realm::close);
        });
    }

    public Observable<Alias> getAlias(String aliasId) {
        return Observable.defer(getFirst(aliasId, RealmAlias.class));
    }

    public Observable<List<Alias>> persistAliases(List<Alias> aliases) {
        return Observable.from(aliases).flatMap(this::persist).toList();
    }

    public Observable<Alias> persist(Alias alias) {
        return toJson(alias).compose(persistJson(RealmAlias.class, alias));
    }

    /****************
     * ChatEvent
     ****************/

    /**
     * Returns the most recent ChatEvent for the ChatRoom with the given id.
     */
    public Observable<ChatEvent> getMostRecentChatEventForChatRoom(String chatRoomId) {
        return Observable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            return Observable.just(realm.copyFromRealm(realm.where(RealmChatEvent.class)
                    .equalTo("alias.chatRoomId", chatRoomId)
                    .findAllSorted("updatedAt", Sort.DESCENDING)
                    .first()))
                    .doAfterTerminate(realm::close)
                    .compose(toValue(ChatEvent.class));
        });
    }

    /**
     * Returns a list of at most |limit| ChatEvents that are the most recent
     * ChatEvents for the ChatRoom with the given id.
     */
    public Observable<List<ChatEvent>> getMostRecentChatEventsForChatRoom(String chatRoomId, int limit) {
        return Observable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            return Observable.just(realm.copyFromRealm(realm.where(RealmChatEvent.class)
                    .equalTo("alias.chatRoomId", chatRoomId)
                    .findAllSorted("updatedAt", Sort.DESCENDING)))
                    .doAfterTerminate(realm::close)
                    .map(results -> results.subList(0, Math.min(results.size(), limit)))
                    .flatMap(Observable::from)
                    .map(RealmChatEvent::toValue)
                    .toList();
        });
    }

    public Observable<ChatEvent> getChatEvent(String chatEventId) {
        return Observable.defer(getFirst(chatEventId, RealmChatEvent.class));
    }

    public Observable<List<ChatEvent>> persistChatEvents(List<ChatEvent> chatEvents) {
        return Observable.from(chatEvents).flatMap(this::persist).toList();
    }

    public Observable<ChatEvent> persist(ChatEvent chatEvent) {
        return toJson(chatEvent).compose(persistJson(RealmChatEvent.class, chatEvent));
    }

    /****************
     * ChatRoom
     ****************/

    public Observable<ChatRoom> getChatRoom(String chatRoomId) {
        return Observable.defer(getFirst(chatRoomId, RealmChatRoom.class));
    }

    public Observable<ChatRoom> persist(ChatRoom chatRoom) {
        return toJson(chatRoom).compose(persistJson(RealmChatRoom.class, chatRoom));
    }

    /****************
     * User
     ****************/

    public Observable<User> getUser(String userId) {
        return Observable.defer(getFirst(userId, RealmUser.class));
    }

    public Observable<User> persist(User user) {
        return toJson(user).compose(persistJson(RealmUser.class, user));
    }

    /****************
     * ChatRoomInfo
     ****************/

    public Observable<List<ChatRoomInfo>> getChatRoomInfos(String userId) {
        return Observable.defer(() -> {
            Realm realm = Realm.getDefaultInstance();
            List<RealmAlias> aliases = realm.copyFromRealm(
                    realm.where(RealmAlias.class)
                            .equalTo("userId", userId)
                            .equalTo("active", true)
                            .findAll());

            return Observable.from(aliases).map(RealmAlias::toValue).flatMap(a ->
                    Observable.zip(getChatRoom(a.chatRoomId()),
                            Observable.just(a),
                            getMostRecentChatEventForChatRoom(a.chatRoomId()),
                            ChatRoomInfo::create))
                    .toList()
                    .doOnNext(infos -> Timber.v("cached infos: " + infos))
                    .doAfterTerminate(realm::close);
        });
    }

    /**
     * Persist the ChatRoomInfos in |infoList|, switch all active Aliases that aren't in
     * |infoList| for |userId| to inactive. (That's the 'exclusive' part!)
     */
    public Observable<List<ChatRoomInfo>> persistChatRoomInfosForUserExclusive(
            String userId, List<ChatRoomInfo> infoList) {
        return getActiveAliasesForUser(userId)
                .flatMap(Observable::from)
                // Flip all found Aliases to inactive
                .map(alias -> alias.withActive(false))
                .map(this::persist)
                .toList()
                // Persist all updated Aliases to cache.
                .flatMap(Observable::merge)
                .toList()
                // Ignore result, persist list of ChatRoomInfos to cache.
                .flatMap(result -> Observable.from(infoList))
                .flatMap(this::persist)
                .toList();
    }

    public Observable<ChatRoomInfo> persist(ChatRoomInfo chatRoomInfo) {
        return Observable.zip(persist(chatRoomInfo.alias()),
                persist(chatRoomInfo.chatEvent()),
                persist(chatRoomInfo.chatRoom()),
                (a, ce, cr) -> chatRoomInfo);
    }

    /****************
     * Misc
     ****************/

    /**
     * Get the first object of type |clazz| with id |objectId| as
     * its Value representation.
     */
    @SuppressWarnings("unchecked")
    private <T> Func0<Observable<T>> getFirst(String objectId,
                                              Class<? extends RealmObject> clazz) {
        return () -> {
            try {
                Realm realm = Realm.getDefaultInstance();
                return Observable.just(realm.copyFromRealm(realm.where(clazz)
                        .equalTo("objectId", objectId)
                        .findFirst()))
                        .doAfterTerminate(realm::close)
                        .compose(toValue((Class<T>) realmToValue.get(clazz)));
            } catch (Exception e) {
                Timber.e("couldn't grab first " + clazz + " for " + objectId + ": " + e);
                return Observable.error(e);
            }
        };
    }

    /**
     * Takes an Observable, saves the string that it outputs to disk
     * as type |clazz| and emits |returnValue|.
     */
    private <T> Observable.Transformer<String, T> persistJson(Class clazz, T returnValue) {
        return o -> o
                .doOnNext(s -> Timber.v("persisting: " + s))
                .doOnNext(saveToDisk(clazz))
                .map(s -> returnValue);
    }

    /**
     * Returns an Action1 that saves a JSON string to Realm.
     */
    private Action1<String> saveToDisk(Class clazz) {
        return json -> {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.createOrUpdateObjectFromJson(clazz, json);
            realm.commitTransaction();
            realm.close();
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

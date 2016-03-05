package com.tonyjhuang.cheddar.api;

import android.util.Log;

import com.parse.ParseUser;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.MessageEvent;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.parse.ParseObservable;

import static com.tonyjhuang.cheddar.api.MessageApi.PUBKEY;
import static com.tonyjhuang.cheddar.api.MessageApi.SUBKEY;


@EBean(scope = EBean.Scope.Singleton)
public class CheddarApi {

    private static final String TAG = CheddarApi.class.getSimpleName();
    private static final String PASSWORD = "password";

    @Bean
    MessageApi messageApi;

    // Keeps track of where we are while paging through the message history.
    private long replayPagerToken = -1;

    public CheddarApi() {
    }

    public Observable<ParseUser> getCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "registering new user");
            return registerNewUser()
                    .flatMap((user) -> ParseObservable.logIn(user.getUsername(), PASSWORD));
        } else {
            Log.d(TAG, "returning user: " + currentUser.getObjectId());
            return Observable.just(currentUser);
        }
    }

    public Observable<Void> logout() {
        return ParseObservable.logOut();
    }

    public Observable<Alias> getAlias(String aliasId) {
        return ParseObservable.get(Alias.class, aliasId);
    }

    public Observable<ParseUser> registerNewUser() {
        return ParseObservable.callFunction("registerNewUser", new HashMap<>());
    }

    public Observable<Map<String, Object>> getDefaultParams(ParseUser user) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getObjectId());
        params.put("subkey", SUBKEY);
        params.put("pubkey", PUBKEY);
        return Observable.just(params);
    }

    public Observable<Alias> joinNextAvailableChatRoom(int maxOccupancy) {
        return getCurrentUser()
                .flatMap(this::getDefaultParams)
                .doOnNext((params) -> params.put("maxOccupancy", maxOccupancy))
                .flatMap(params -> ParseObservable.callFunction("joinNextAvailableChatRoom", params));
    }

    public Observable<Alias> leaveChatRoom(String aliasId) {
        Map<String, Object> params = new HashMap<>();
        params.put("aliasId", aliasId);
        params.put("subkey", SUBKEY);
        params.put("pubkey", PUBKEY);
        return ParseObservable.callFunction("leaveChatRoom", params);
    }

    public Observable<Void> sendMessage(String aliasId, String body) {
        return getCurrentUser()
                .flatMap(this::getDefaultParams)
                .doOnNext((params) -> {
                    params.put("aliasId", aliasId);
                    params.put("body", body);
                    Log.e(TAG, "Calling with " + params);
                })
                .flatMap(params -> ParseObservable.callFunction("sendMessage", params))
                .map((object) -> null);
    }


    public Observable<MessageEvent> getMessageStream(String aliasId) {
        Log.e(TAG, "getMessageStream");
        return getAlias(aliasId)
                .map(Alias::getChatRoomId)
                .flatMap(messageApi::subscribe)
                .cast(JSONObject.class)
                .flatMap(MessageEventParser::parse);
    }

    public Observable<Void> endMessageStream(String aliasId) {
        return getAlias(aliasId)
                .map(Alias::getChatRoomId)
                .flatMap(messageApi::unsubscribe);
    }

    // Resets our replayPagerToken. Should be called if you'd like to start replaying messages
    // from the beginning.
    public void resetReplayMessageEvents() {
        replayPagerToken = -1;
    }

    /**
     * Retrieve past MessageEvents, sorted from newest to oldest.
     */
    public Observable<List<MessageEvent>> replayMessageEvents(String aliasId, int count) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("aliasId", aliasId);
        params.put("count", count);
        params.put("subkey", SUBKEY);

        if (replayPagerToken != -1) {
            params.put("startTimeToken", replayPagerToken);
        }

        Log.e(TAG, "replayMessageEvents | " + params);

        // Returns:
        // {"events":[{event}, {event}],
        //   "startTimeToken": "00000",
        //   "endTimeToken": "00000"}
        return ParseObservable.callFunction("replayEvents", params)
                .cast(HashMap.class)
                .doOnNext(response -> replayPagerToken = Long.valueOf((String) response.get("startTimeToken")))
                .doOnNext(response -> Log.e(TAG, response.toString()))
                .map(map -> (List<Object>) map.get("events"))
                .flatMap(Observable::from)
                .cast(HashMap.class)
                .flatMap((HashMap map) -> MessageEventParser.parse(sanitize(map)))
                .toList()
                .doOnNext(Collections::reverse);
    }

    /**
     * Turns all ParseObjects in |map| into json objects
     */
    public JSONObject sanitize(HashMap map) {
        for (Object key : map.keySet()) {
            Object o = map.get(key);
            if (o instanceof HashMap) {
                sanitize((HashMap) o);
            } else if (o instanceof Alias) {
                map.put(key, ((Alias) o).toJson());
            }
        }
        return new JSONObject(map);
    }

    public Observable<Object> registerForPushNotifications(String aliasId, String registrationToken) {
        return getAlias(aliasId).flatMap(alias ->
                messageApi.registerForPushNotifications(alias.getChatRoomId(), registrationToken));
    }


    public Observable<Object> unregisterForPushNotifications(String aliasId, String registrationToken) {
        return getAlias(aliasId).flatMap(alias ->
                messageApi.unregisterForPushNotifications(alias.getChatRoomId(), registrationToken));
    }
}

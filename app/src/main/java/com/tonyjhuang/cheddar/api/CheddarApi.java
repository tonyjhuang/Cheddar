package com.tonyjhuang.cheddar.api;

import android.util.Log;

import com.parse.ParseUser;
import com.tonyjhuang.cheddar.api.feedback.FeedbackApi;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.api.models.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.JSONParseObject;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rx.Observable;
import rx.parse.ParseObservable;
import timber.log.Timber;

import static com.tonyjhuang.cheddar.api.MessageApi.PUBKEY;
import static com.tonyjhuang.cheddar.api.MessageApi.SUBKEY;


@EBean(scope = EBean.Scope.Singleton)
public class CheddarApi {

    private static final String TAG = CheddarApi.class.getSimpleName();
    private static final String PASSWORD = "password";

    @Bean
    MessageApi messageApi;

    @Bean
    FeedbackApi feedbackApi;

    // Keeps track of where we are while paging through the message history.
    private long replayPagerToken = -1;

    public CheddarApi() {
    }

    //******************************************************
    //                Users
    //******************************************************

    public Observable<ParseUser> getCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            return registerNewUser()
                    .flatMap((user) -> ParseObservable.logIn(user.getUsername(), PASSWORD));
        } else {
            return Observable.just(currentUser);
        }
    }

    public Observable<ParseUser> fetchCurrentUser() {
        return getCurrentUser().map(ParseUser::getObjectId)
                .flatMap(userId -> ParseObservable.get(ParseUser.class, userId));
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

    //******************************************************
    //                Chat Rooms
    //******************************************************

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

    public Observable<List<Alias>> getActiveAliases(String chatRoomId) {
        Map<String, Object> params = new HashMap<>();
        params.put("chatRoomId", chatRoomId);
        return ParseObservable.callFunction("getActiveAliases", params);
    }

    public Observable<List<ChatRoomInfo>> getChatRooms() {
        return getChatRoomsHelper().flatMap(Observable::from)
                .doOnNext(o -> Timber.d(o.toString()))
                .map(this::sanitize)
                .doOnNext(o -> Timber.d(o.toString()))
                .flatMap(CheddarParser::parseChatRoomInfoRx)
                .toList();
    }

    // hack to cast the result as a list<hashmap>
    // ugh this is why i want to kill parseobservable :'(
    public Observable<List<HashMap>> getChatRoomsHelper() {
        return getCurrentUser()
                .map(ParseUser::getObjectId)
                .map(userId -> {
                    Map<String, Object> params = new HashMap<>();
                    params.put("userId", userId);
                    return params;
                })
                .doOnNext(params -> Timber.d(params.toString()))
                .flatMap(params -> ParseObservable.callFunction("getChatRooms", params));
    }

    //******************************************************
    //                Messages
    //******************************************************

    public Observable<Void> sendMessage(String aliasId, String body) {
        return getCurrentUser()
                .flatMap(this::getDefaultParams)
                .doOnNext((params) -> {
                    params.put("aliasId", aliasId);
                    params.put("body", body);
                    params.put("messageId", UUID.randomUUID().toString());
                    Log.e(TAG, "Calling with " + params);
                })
                .flatMap(params -> ParseObservable.callFunction("sendMessage", params))
                .doOnError(error -> Log.e(TAG, "failed to send message: " + error))
                .map((object) -> null);
    }

    public Observable<ChatEvent> getMessageStream(String aliasId) {
        Log.e(TAG, "getMessageStream");
        return getAlias(aliasId)
                .map(Alias::getChatRoomId)
                .flatMap(messageApi::subscribe)
                .cast(JSONObject.class)
                // Continue past any Exceptions thrown in parseChatEventRx.
                .flatMap(CheddarParser::parseChatEventRxSkippable);
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
     * Retrieve past ChatEvents, sorted from newest to oldest.
     */
    public Observable<List<ChatEvent>> replayChatEvents(String aliasId, int count) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("aliasId", aliasId);
        params.put("count", count);
        params.put("subkey", SUBKEY);

        if (replayPagerToken != -1) {
            params.put("startTimeToken", replayPagerToken);
        }

        Log.e(TAG, "replayChatEvents | " + params);

        return ParseObservable.callFunction("replayEvents", params).cast(HashMap.class)
                .doOnNext(response -> replayPagerToken = Long.valueOf((String) response.get("startTimeToken")))
                .compose(parseChatEvents());
    }

    public Observable<List<ChatEvent>> replayChatEvents(String aliasId, Date start, Date end) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("aliasId", aliasId);
        params.put("count", 999);
        params.put("subkey", SUBKEY);
        params.put("startTimeToken", start.getTime() * 10000);
        params.put("endTimeToken", end.getTime() * 10000);

        Log.e(TAG, "replayChatEvents | " + params);

        return ParseObservable.callFunction("replayEvents", params).cast(HashMap.class)
                .compose(parseChatEvents());
    }

    private Observable.Transformer<HashMap, List<ChatEvent>> parseChatEvents() {
        // Returns:
        // {"events":[{event}, {event}],
        //   "startTimeToken": "00000",
        //   "endTimeToken": "00000"}
        return o -> o
                .doOnNext(response -> Log.e(TAG, response.toString()))
                .map(map -> (List<Object>) map.get("events"))
                .flatMap(Observable::from)
                .cast(HashMap.class)
                .flatMap((HashMap map) -> CheddarParser.parseChatEventRxSkippable(sanitize(map)))
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
            } else if (o instanceof JSONParseObject) {
                map.put(key, ((JSONParseObject) o).toJson());
            }
        }
        return new JSONObject(map);
    }

    //******************************************************
    //                Miscellaneous
    //******************************************************

    /**
     * Send Feedback from a non-chat context.
     */
    public Observable<String> sendFeedback(String versionName, String userId, String name, String feedback) {
        FeedbackApi.FeedbackInfo.Builder builder = new FeedbackApi.FeedbackInfo.Builder()
                .setVersionName(versionName)
                .setUserId(userId)
                .setName(name)
                .setFeedback(feedback);
        return feedbackApi.sendFeedback(builder.build());
    }

    /**
     * Send feedback from the chat context.
     */
    public Observable<String> sendFeedback(String versionName, Alias alias, String name, String feedback) {
        FeedbackApi.FeedbackInfo.Builder builder = new FeedbackApi.FeedbackInfo.Builder()
                .setVersionName(versionName)
                .setUserId(alias.getUserId())
                .setChatRoomId(alias.getChatRoomId())
                .setAliasName(alias.getName())
                .setName(name)
                .setFeedback(feedback);
        return feedbackApi.sendFeedback(builder.build());
    }
}

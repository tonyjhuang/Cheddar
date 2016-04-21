package com.tonyjhuang.cheddar.api;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.tonyjhuang.cheddar.api.feedback.FeedbackApi;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.ParseToValueTranslator;
import com.tonyjhuang.cheddar.api.models.parse.JSONParseObject;
import com.tonyjhuang.cheddar.api.models.parse.ParseAlias;
import com.tonyjhuang.cheddar.api.models.parse.ParseChatEvent;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.network.ParseApi;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.parse.ParseObservable;
import timber.log.Timber;

import static com.tonyjhuang.cheddar.api.MessageApi.PUBKEY;
import static com.tonyjhuang.cheddar.api.MessageApi.SUBKEY;


@EBean(scope = EBean.Scope.Singleton)
public class CheddarApi {

    private static final String PASSWORD = "password";
    private static final int GROUP_CHAT_SIZE = 5;

    @Bean
    MessageApi messageApi;

    @Bean
    FeedbackApi feedbackApi;

    @Bean
    ParseApi parseApi;

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
        return ParseObservable.get(ParseAlias.class, aliasId)
                .map(ParseToValueTranslator::toAlias);
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

    public Observable<ParseAlias> joinNextAvailableGroupChatRoom() {
        return getCurrentUser()
                .flatMap(this::getDefaultParams)
                .doOnNext((params) -> params.put("maxOccupancy", GROUP_CHAT_SIZE))
                .flatMap(params -> ParseObservable.callFunction("joinNextAvailableChatRoom", params));
    }

    public Observable<ParseAlias> joinNextAvailableChatRoom(int maxOccupancy) {
        return getCurrentUser()
                .flatMap(this::getDefaultParams)
                .doOnNext((params) -> params.put("maxOccupancy", maxOccupancy))
                .flatMap(params -> ParseObservable.callFunction("joinNextAvailableChatRoom", params));
    }

    public Observable<ParseAlias> leaveChatRoom(String aliasId) {
        Map<String, Object> params = new HashMap<>();
        params.put("aliasId", aliasId);
        params.put("subkey", SUBKEY);
        params.put("pubkey", PUBKEY);
        return ParseObservable.callFunction("leaveChatRoom", params);
    }

    public Observable<List<Alias>> getActiveAliases(String chatRoomId) {
        return getActiveAliasesHelper(chatRoomId)
                .flatMap(Observable::from)
                .map(ParseToValueTranslator::toAlias)
                .toList();
    }

    //TODO: change to return json
    private Observable<List<ParseAlias>> getActiveAliasesHelper(String chatRoomId) {
        Map<String, Object> params = new HashMap<>();
        params.put("chatRoomId", chatRoomId);
        return ParseObservable.callFunction("getActiveAliases", params);
    }

    public Observable<List<ChatRoomInfo>> getChatRooms() {
        return getCurrentUser().map(ParseUser::getObjectId)
                .flatMap(parseApi::getChatRooms);
    }

    //******************************************************
    //                Messages
    //******************************************************

    /**
     * Returns two ChatEvent Messages. The first is a placeholder
     * Message that can be displayed in the UI until the real Message
     * is confirmed sent. The second is the same as the first that can
     * be treated as a 'sent confirmation'.
     */
    public Observable<ChatEvent> sendMessage(String messageId, String aliasId, String body) {
        return getAlias(aliasId)
                .map(alias -> ChatEvent.createPlaceholderMessage(messageId, alias, body))
                .flatMap(message -> Observable.just(message).concatWith(sendMessageHelper(message)));
    }

    private Observable<ChatEvent> sendMessageHelper(ChatEvent message) {
        return getCurrentUser()
                .flatMap(this::getDefaultParams)
                .doOnNext((params) -> {
                    params.put("aliasId", message.alias().metaData().objectId());
                    params.put("body", message.body());
                    params.put("messageId", message.metaData().objectId());
                    Timber.e("Calling with " + params);
                })
                .flatMap(params -> ParseObservable.callFunction("sendMessage", params))
                .doOnError(error -> Timber.e("failed to send message: " + error))
                .map(result -> message);
    }

    public Observable<ChatEvent> getMessageStream(String aliasId) {
        return getAlias(aliasId)
                .map(Alias::chatRoomId)
                .flatMap(messageApi::subscribe)
                .cast(JSONObject.class)
                // Continue past any Exceptions thrown in parseChatEventRx.
                .flatMap(CheddarParser::parseChatEventRxSkippable)
                .map(ParseToValueTranslator::toChatEvent);
    }

    public Observable<Void> endMessageStream(String aliasId) {
        return getAlias(aliasId)
                .map(Alias::chatRoomId)
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

        return ParseObservable.callFunction("replayEvents", params).cast(HashMap.class)
                .doOnNext(response -> replayPagerToken = Long.valueOf((String) response.get("startTimeToken")))
                .compose(parseChatEvents())
                .flatMap(Observable::from)
                .map(ParseToValueTranslator::toChatEvent)
                .toList();
    }

    public Observable<List<ChatEvent>> replayChatEvents(String aliasId, Date start, Date end) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("aliasId", aliasId);
        params.put("count", 999);
        params.put("subkey", SUBKEY);
        params.put("startTimeToken", start.getTime() * 10000);
        params.put("endTimeToken", end.getTime() * 10000);

        return ParseObservable.callFunction("replayEvents", params)
                .cast(HashMap.class)
                .compose(parseChatEvents())
                .flatMap(Observable::from)
                .map(ParseToValueTranslator::toChatEvent)
                .toList();
    }

    private Observable.Transformer<HashMap, List<ParseChatEvent>> parseChatEvents() {
        // Returns:
        // {"events":[{event}, {event}],
        //   "startTimeToken": "00000",
        //   "endTimeToken": "00000"}
        return o -> o
                .doOnNext(response -> Timber.i(response.toString()))
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
                .setUserId(alias.userId())
                .setChatRoomId(alias.chatRoomId())
                .setAliasName(alias.name())
                .setName(name)
                .setFeedback(feedback);
        return feedbackApi.sendFeedback(builder.build());
    }
}

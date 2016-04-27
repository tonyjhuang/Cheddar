package com.tonyjhuang.cheddar.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tonyjhuang.cheddar.api.feedback.FeedbackApi;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.api.network.ParseApi;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.parse.ParseObservable;
import timber.log.Timber;


/**
 * Data abstraction layer. Handles getting data from the network and
 * manages the cache.
 */
@EBean(scope = EBean.Scope.Singleton)
public class CheddarApi {

    private static final String PASSWORD = "password";
    private final Gson gson = createGson();
    @Bean
    MessageApi messageApi;
    @Bean
    FeedbackApi feedbackApi;
    @Bean
    ParseApi parseApi;
    // Keeps track of where we are while paging through the message history.
    private Date replayPagerToken = null;

    private User currentUser = null;

    public CheddarApi() {
    }

    private static Gson createGson() {
        return new GsonBuilder().create();
    }

    //******************************************************
    //                Users
    //******************************************************

    public Observable<User> getCurrentUser() {
        return Observable.defer(() -> {
            if(currentUser != null) {
                return Observable.just(currentUser);
            } else {
                return registerNewUser().doOnNext(user -> Timber.d("user: " + user));
            }
        });
    }

    public Observable<Void> logout() {
        return ParseObservable.logOut();
    }

    public Observable<Alias> getAlias(String aliasId) {
        return parseApi.findAlias(aliasId);
    }

    public Observable<User> registerNewUser() {
        return parseApi.registerNewUser().doOnNext(user -> currentUser = user);
    }

    //******************************************************
    //                Chat Rooms
    //******************************************************

    public Observable<Alias> joinGroupChatRoom() {
        return getCurrentUser().map(User::objectId)
                .flatMap(parseApi::joinGroupChatRoom);
    }


    public Observable<Alias> leaveChatRoom(String aliasId) {
        return parseApi.leaveChatRoom(aliasId);
    }

    public Observable<List<Alias>> getActiveAliases(String chatRoomId) {
        return parseApi.getActiveAliases(chatRoomId);
    }

    public Observable<List<ChatRoomInfo>> getChatRooms() {
        return getCurrentUser().map(User::objectId)
                .flatMap(parseApi::getChatRooms);
    }

    //******************************************************
    //                Messages
    //******************************************************

    /**
     * Sends a ChatEvent to the network. Create new Messages
     * using ChatEvent.createPlaceholderMessage.
     */
    public Observable<ChatEvent> sendMessage(ChatEvent chatEvent) {
        return parseApi.sendMessage(chatEvent.alias().objectId(),
                chatEvent.body(), chatEvent.objectId());
    }

    public Observable<ChatEvent> getMessageStream(String aliasId) {
        return getAlias(aliasId)
                .map(Alias::chatRoomId)
                .flatMap(messageApi::subscribe)
                .doOnNext(o -> Timber.i(o.toString()))
                .cast(JSONObject.class)
                // Continue past any Exceptions thrown in parseChatEventRx.
                .flatMap(this::parseChatEventOrSkip);
    }

    /**
     * Turns a JSONObject into a ChatEvent or emits nothing if
     * the json is unparseable.
     */
    private Observable<ChatEvent> parseChatEventOrSkip(JSONObject o) {
        return Observable.just(gson.fromJson(o.toString(), ChatEvent.class))
                .doOnError(error -> {
                    Timber.e("uh oh, couldn't parse: " + o);
                    Timber.e(error.toString());
                })
                .onExceptionResumeNext(Observable.empty());
    }

    public Observable<Void> endMessageStream(String aliasId) {
        return getAlias(aliasId)
                .map(Alias::chatRoomId)
                .flatMap(messageApi::unsubscribe);
    }

    // Resets our replayPagerToken. Should be called if you'd like to start replaying messages
    // from the beginning.
    public void resetReplayChatEvents() {
        replayPagerToken = null;
    }

    /**
     * Retrieve past ChatEvents, sorted from newest to oldest.
     */
    public Observable<List<ChatEvent>> replayChatEvents(String aliasId, int count) {
        return parseApi.pageChatEvents(aliasId, count, replayPagerToken)
                .doOnNext(response -> replayPagerToken = response.startTimeToken)
                .map(response -> response.chatEvents)
                .doOnNext(Collections::reverse)
                .doOnNext(r -> Timber.d("chatEvents: " + r));
    }

    public Observable<List<ChatEvent>> replayChatEvents(String aliasId, Date start, Date end) {
        return parseApi.getChatEventsInRange(aliasId, start, end);
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

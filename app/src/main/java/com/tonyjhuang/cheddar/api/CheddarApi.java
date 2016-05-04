package com.tonyjhuang.cheddar.api;

import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.api.cache.CacheApi;
import com.tonyjhuang.cheddar.api.feedback.FeedbackApi;
import com.tonyjhuang.cheddar.api.message.MessageApi;
import com.tonyjhuang.cheddar.api.message.MessageApiChatEventHolder;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.api.network.ParseApi;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import rx.Observable;
import timber.log.Timber;


/**
 * Data abstraction layer. Handles getting data from the network and
 * manages the cache.
 */
@EBean(scope = EBean.Scope.Singleton)
public class CheddarApi {
    @Bean
    MessageApi messageApi;
    @Bean
    FeedbackApi feedbackApi;
    @Bean
    ParseApi parseApi;
    @Bean
    CacheApi cacheApi;
    @Pref
    CheddarPrefs_ prefs;

    // Keeps track of where we are while paging through the message history.
    private Date replayPagerToken = null;

    public CheddarApi() {
    }

    //******************************************************
    //                Users
    //******************************************************

    public Observable<User> getCurrentUser() {
        return Observable.defer(() -> {
            if (!prefs.currentUserId().getOr("").isEmpty()) {
                Timber.v("cached current user");
                return cacheApi.getUser(prefs.currentUserId().get())
                        .doOnNext(user -> Timber.v("cached user: " + user))
                        .doOnError(error -> Timber.e(error.toString()))
                        .doOnError(error -> prefs.currentUserId().remove())
                        .onExceptionResumeNext(getCurrentUser());
            } else {
                Timber.v("registering new user");
                return parseApi.registerNewUser()
                        .doOnNext(user -> prefs.currentUserId().put(user.objectId()))
                        .doOnNext(user -> Timber.d("registered user: " + user))
                        .flatMap(user -> cacheApi.persist(user));
            }
        });
    }

    public Observable<Void> logout() {
        return Observable.defer(() -> {
            prefs.currentUserId().remove();
            return Observable.just(null);
        });
    }

    //******************************************************
    //                Aliases
    //******************************************************

    public Observable<Alias> getAliasForChatRoom(String chatRoomId) {
        return getCurrentUser().map(User::objectId)
                .flatMap(userId -> cacheApi.getAliasForChatRoom(userId, chatRoomId));
    }

    public Observable<Alias> fetchAlias(String aliasId) {
        return parseApi.findAlias(aliasId)
                .doOnNext(alias -> Timber.d("returning alias: " + alias))
                .flatMap(cacheApi::persist)
                .doOnNext(alias -> Timber.d("persisted.."));
    }

    public Observable<Alias> getAlias(String aliasId) {
        return cacheApi.getAlias(aliasId)
                // Fetch Alias from network if it doesn't exist in the cache.
                .onExceptionResumeNext(fetchAlias(aliasId));
    }

    //******************************************************
    //                ChatRooms
    //******************************************************

    public Observable<Alias> joinGroupChatRoom() {
        return getCurrentUser().map(User::objectId)
                .flatMap(parseApi::joinGroupChatRoom);
    }


    public Observable<Alias> leaveChatRoom(String aliasId) {
        return parseApi.leaveChatRoom(aliasId)
                .flatMap(cacheApi::persist);
    }

    public Observable<List<Alias>> getActiveAliases(String chatRoomId) {
        return parseApi.getActiveAliases(chatRoomId)
                .flatMap(cacheApi::persistAliases);
    }

    public Observable<List<ChatRoomInfo>> getChatRooms() {
        return getCurrentUser().map(User::objectId)
                .flatMap(userId -> Observable.concat(
                        cacheApi.getChatRoomInfos(userId)
                                .compose(sortChatRoomInfoList())
                                .doOnNext(infos -> Timber.i("cached: " + infos.get(0).chatEvent()))
                                .doOnError(error -> Timber.e(error, "couldn't get cached infolist"))
                                .onExceptionResumeNext(Observable.empty()),
                        parseApi.getChatRooms(userId).flatMap(cacheApi::persistChatRoomInfos)
                                .compose(sortChatRoomInfoList())
                                .doOnNext(infos -> Timber.i("network: " + infos.get(0).chatEvent()))));
    }

    private Observable.Transformer<List<ChatRoomInfo>, List<ChatRoomInfo>> sortChatRoomInfoList() {
        return o -> o.flatMap(Observable::from)
                .toSortedList((i1, i2) -> i2.chatEvent().updatedAt().compareTo(i1.chatEvent().updatedAt()));
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
                chatEvent.body(), chatEvent.objectId())
                .flatMap(cacheApi::persist);
    }

    public Observable<ChatEvent> getMessageStream(String aliasId) {
        Timber.d("getMessageStream");
        return getAlias(aliasId).map(Alias::chatRoomId)
                .flatMap(messageApi::subscribe)
                .filter(o -> o instanceof MessageApiChatEventHolder)
                .cast(MessageApiChatEventHolder.class)
                .map(o -> o.chatEvent)
                .flatMap(cacheApi::persist);
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
    public Observable<List<ChatEvent>> replayChatEvents(String aliasId, int limit) {
        return Observable.concat(
                getAlias(aliasId).map(Alias::chatRoomId)
                        .flatMap(chatRoomId -> cacheApi.getMostRecentChatEventsForChatRoom(chatRoomId, limit))
                        .doOnNext(chatEvents -> Timber.i("cached chatEvents: %d", chatEvents.size())),
                parseApi.pageChatEvents(aliasId, limit, replayPagerToken)
                        .doOnNext(response -> replayPagerToken = response.startTimeToken)
                        .map(response -> response.chatEvents)
                        .doOnNext(Collections::reverse)
                        .doOnNext(chatEvents -> Timber.i("network chatEvents: %d", chatEvents.size()))
                        .flatMap(cacheApi::persistChatEvents));
    }

    public Observable<List<ChatEvent>> replayChatEvents(String aliasId, Date start, Date end) {
        return parseApi.getChatEventsInRange(aliasId, start, end)
                .flatMap(cacheApi::persistChatEvents);
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

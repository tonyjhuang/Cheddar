package com.tonyjhuang.cheddar.api;

import android.content.Context;

import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.api.cache.CacheApi;
import com.tonyjhuang.cheddar.api.message.MessageApi;
import com.tonyjhuang.cheddar.api.message.MessageApiChatEventHolder;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoom;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.api.network.ParseApi;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
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
    @RootContext
    Context context;
    @Bean
    MessageApi messageApi;
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
    //              ! DEBUG !
    //******************************************************

    public Observable<Void> debugReset() {
        prefs.currentUserId().put("");
        prefs.onboardShown().put(false);
        prefs.userEmailVerified().put(false);
        prefs.lastVersionName().put("");
        prefs.unreadMessages().put("");
        prefs.pushChannels().put("");
        prefs.gcmRegistrationToken().put("");
        return parseApi.deleteCurrentUser()
                .flatMap(aVoid -> cacheApi.debugReset());
    }

    //******************************************************
    //                Users
    //******************************************************

    /**
     * Retrieve the User with the specified id from the network.
     */
    public Observable<User> fetchUser(String userId) {
        return parseApi.findUser(userId)
                .flatMap(cacheApi::persist);
    }

    public Observable<User> registerNewUser(String email, String password, String registrationCode) {
        return parseApi.registerUser(email, password, registrationCode)
                .doOnNext(user -> prefs.currentUserId().put(user.objectId()))
                .flatMap(cacheApi::persist);
    }


    //******************************************************
    //                Current User
    //******************************************************

    /**
     * Gets the current user's id or else emits a NoCurrentUserException.
     */
    private Observable<String> getCurrentUserId() {
        return Observable.defer(() -> {
            String currentUserId = prefs.currentUserId().getOr("");
            if (!currentUserId.isEmpty()) {
                return Observable.just(currentUserId);
            } else {
                return Observable.error(new NoCurrentUserException());
            }
        });
    }

    public Observable<User> getCurrentUser() {
        return getCurrentUserId()
                .flatMap(currentUserId -> cacheApi.getUser(currentUserId)
                        .onExceptionResumeNext(fetchUser(currentUserId)));
    }

    public Observable<User> resendCurrentUserVerificationEmail() {
        return getCurrentUserId().flatMap(parseApi::resendVerificationEmail);
    }

    public Observable<Boolean> isCurrentUserEmailVerified() {
        return getCurrentUserId()
                .flatMap(parseApi::isUserEmailVerified)
                .onExceptionResumeNext(Observable.just(false))
                .doOnNext(emailVerified -> prefs.userEmailVerified().put(emailVerified));
    }

    public Observable<User> login(String email, String password) {
        return parseApi.login(email, password)
                .flatMap(cacheApi::persist)
                .doOnNext(user -> {
                    prefs.currentUserId().put(user.objectId());
                    prefs.userEmailVerified().put(user.emailVerified());
                    prefs.unreadMessages().put("");
                    prefs.pushChannels().put("");
                });
    }

    /**
     * Unregister from all push notifications, logout current user.
     */
    public Observable<Void> logoutCurrentUser() {
        return Observable.defer(() -> {
            // Log user out of prefs.
            prefs.currentUserId().put("");
            prefs.userEmailVerified().put(false);
            prefs.unreadMessages().put("");
            prefs.pushChannels().put("");
            return parseApi.logout();
        });
    }


    //******************************************************
    //                Aliases
    //******************************************************

    public Observable<Alias> fetchAlias(String aliasId) {
        return parseApi.findAlias(aliasId)
                .flatMap(cacheApi::persist);
    }

    public Observable<Alias> getAlias(String aliasId) {
        return cacheApi.getAlias(aliasId)
                // Fetch Alias from network if it doesn't exist in the cache.
                .onExceptionResumeNext(fetchAlias(aliasId));
    }

    public Observable<Alias> getAliasForChatRoom(String chatRoomId) {
        return getCurrentUser().map(User::objectId)
                .flatMap(userId -> cacheApi.getAliasForChatRoom(userId, chatRoomId));
    }

    //******************************************************
    //                ChatRooms
    //******************************************************

    public Observable<ChatRoom> fetchChatRoom(String chatRoomId) {
        return parseApi.findChatRoom(chatRoomId).flatMap(cacheApi::persist);
    }

    public Observable<ChatRoom> getChatRoom(String chatRoomId) {
        return cacheApi.getChatRoom(chatRoomId)
                .onExceptionResumeNext(fetchChatRoom(chatRoomId));
    }

    public Observable<Alias> joinGroupChatRoom() {
        return getCurrentUser().map(User::objectId)
                .flatMap(parseApi::joinGroupChatRoom)
                .flatMap(cacheApi::persist)
                .flatMap(alias -> Observable.defer(() ->
                        // Cache ChatRoom after joining.
                        getChatRoom(alias.chatRoomId())
                                .flatMap(cacheApi::persist)
                                .map(chatRoom -> alias)));
    }

    public Observable<Alias> leaveChatRoom(String aliasId) {
        return parseApi.leaveChatRoom(aliasId)
                .flatMap(cacheApi::persist);
    }

    public Observable<List<Alias>> getActiveAliases(String chatRoomId) {
        return parseApi.getActiveAliases(chatRoomId)
                .flatMap(cacheApi::persistAliases);
    }

    public Observable<List<ChatRoomInfo>> getChatRoomInfos() {
        return getCurrentUser().map(User::objectId)
                .flatMap(userId -> Observable.concat(
                        cacheApi.getChatRoomInfos(userId)
                                .compose(sortChatRoomInfoList())
                                .doOnNext(infos -> Timber.v("cached: " + infos.size()))
                                .onExceptionResumeNext(Observable.empty()),
                        // Network call.
                        fetchChatRoomInfos(userId)));
    }

    public Observable<List<ChatRoomInfo>> fetchChatRoomInfos(String userId) {
        return parseApi.getChatRooms(userId)
                .doOnNext(infos -> Timber.v("network: " + infos.size()))
                .flatMap(infos -> cacheApi.persistChatRoomInfosForUserExclusive(userId, infos))
                .compose(sortChatRoomInfoList());
    }

    private Observable.Transformer<List<ChatRoomInfo>, List<ChatRoomInfo>> sortChatRoomInfoList() {
        return o -> o.flatMap(Observable::from)
                .toSortedList((i1, i2) -> i2.chatEvent().updatedAt().compareTo(i1.chatEvent().updatedAt()));
    }

    public Observable<ChatRoom> updateChatRoomName(String aliasId, String name) {
        return parseApi.updateChatRoomName(aliasId, name.trim().replace("\n", ""))
                .flatMap(cacheApi::persist);
    }

    /**
     * Sends a ChatEvent to the network. Create new Messages
     * using ChatEvent.createPlaceholderMessage.
     */
    public Observable<ChatEvent> sendMessage(ChatEvent chatEvent) {
        return parseApi.sendMessage(chatEvent.alias().objectId(), chatEvent.body(), chatEvent.objectId())
                .flatMap(cacheApi::persist);
    }

    //******************************************************
    //                MessageApi
    //******************************************************

    public Observable<ChatEvent> getChatEventStream(String aliasId) {
        return getAlias(aliasId).map(Alias::chatRoomId)
                .flatMap(messageApi::subscribe)
                .filter(o -> o instanceof MessageApiChatEventHolder)
                .cast(MessageApiChatEventHolder.class)
                .map(o -> o.chatEvent)
                .flatMap(cacheApi::persist);
    }

    public Observable<Void> endChatEventStream(String aliasId) {
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
                Observable.defer(() -> {
                    // Retrieve ChatEvents from cache IFF this is the first load.
                    if (replayPagerToken == null) {
                        return getAlias(aliasId).map(Alias::chatRoomId)
                                .flatMap(chatRoomId -> cacheApi.getMostRecentChatEventsForChatRoom(chatRoomId, limit))
                                .compose(sortChatEventList())
                                .doOnNext(chatEvents -> Timber.v("cached chatEvents: %d", chatEvents.size()));
                    } else {
                        return Observable.empty();
                    }
                }),
                parseApi.pageChatEvents(aliasId, limit, replayPagerToken)
                        .doOnNext(response -> replayPagerToken = response.startTimeToken)
                        .map(response -> response.chatEvents)
                        .doOnNext(Collections::reverse)
                        .compose(sortChatEventList())
                        .doOnNext(chatEvents -> Timber.v("network chatEvents: " + chatEvents))
                        .flatMap(cacheApi::persistChatEvents));
    }

    private Observable.Transformer<List<ChatEvent>, List<ChatEvent>> sortChatEventList() {
        return o -> o.flatMap(Observable::from)
                .toSortedList((ce1, ce2) -> ce2.updatedAt().compareTo(ce1.updatedAt()));
    }

    public Observable<List<ChatEvent>> replayChatEvents(String aliasId, Date start, Date end) {
        return parseApi.getChatEventsInRange(aliasId, start, end)
                .flatMap(cacheApi::persistChatEvents);
    }

    //******************************************************
    //                Miscellaneous
    //******************************************************

    /**
     * Send feedback from the chat context.
     */
    public Observable<String> sendFeedback(Alias alias, String feedback) {
        return parseApi.sendFeedback(alias, feedback);
    }

    public Observable<String> registerDifferentSchool(String schoolName, String email) {
        return parseApi.sendChangeSchoolRequest(schoolName, email);
    }

    /**
     * Thrown when a method that requires a logged in user is called and there is none.
     */
    public static class NoCurrentUserException extends Throwable {
    }
}

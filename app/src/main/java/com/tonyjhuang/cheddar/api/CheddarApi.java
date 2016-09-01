package com.tonyjhuang.cheddar.api;

import android.content.Context;

import com.crashlytics.android.Crashlytics;
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

    public static final int MAX_CHAT_ROOMS = 5;

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
                .flatMap(aVoid -> cacheApi.debugReset())
                .doOnError(Crashlytics::logException);
    }

    //******************************************************
    //                Users
    //******************************************************

    /**
     * Retrieve the User with the specified id from the network.
     */
    public Observable<User> fetchUser(String userId) {
        return parseApi.findUser(userId)
                .flatMap(cacheApi::persist)
                .doOnError(Crashlytics::logException);
    }

    public Observable<User> registerNewUser(String email, String password, String registrationCode) {
        return parseApi.registerUser(email, password, registrationCode)
                .doOnNext(user -> prefs.currentUserId().put(user.objectId()))
                .flatMap(cacheApi::persist)
                .doOnError(Crashlytics::logException);
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
        }).doOnError(Crashlytics::logException);
    }

    public Observable<User> getCurrentUser() {
        return getCurrentUserId()
                .flatMap(currentUserId -> cacheApi.getUser(currentUserId)
                        .onExceptionResumeNext(fetchUser(currentUserId)))
                .doOnError(Crashlytics::logException);
    }

    public Observable<User> resendCurrentUserVerificationEmail() {
        return getCurrentUserId().flatMap(parseApi::resendVerificationEmail)
                .doOnError(Crashlytics::logException);
    }

    public Observable<Boolean> isCurrentUserEmailVerified() {
        return getCurrentUserId()
                .flatMap(parseApi::isUserEmailVerified)
                .onExceptionResumeNext(Observable.just(false))
                .doOnNext(emailVerified -> prefs.userEmailVerified().put(emailVerified))
                .doOnError(Crashlytics::logException);
    }

    public Observable<User> login(String email, String password) {
        return parseApi.login(email, password)
                .flatMap(cacheApi::persist)
                .doOnNext(user -> {
                    prefs.currentUserId().put(user.objectId());
                    prefs.userEmailVerified().put(user.emailVerified());
                    prefs.unreadMessages().put("");
                    prefs.pushChannels().put("");
                })
                .doOnError(Crashlytics::logException);
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
        }).doOnError(Crashlytics::logException);
    }


    //******************************************************
    //                Aliases
    //******************************************************

    public Observable<Alias> fetchAlias(String aliasId) {
        return parseApi.findAlias(aliasId)
                .flatMap(cacheApi::persist)
                .doOnError(Crashlytics::logException);
    }

    public Observable<Alias> getAlias(String aliasId) {
        return cacheApi.getAlias(aliasId)
                // Fetch Alias from network if it doesn't exist in the cache.
                .onExceptionResumeNext(fetchAlias(aliasId))
                .doOnError(Crashlytics::logException);
    }

    public Observable<Alias> getAliasForChatRoom(String chatRoomId) {
        return getCurrentUser().map(User::objectId)
                .flatMap(userId -> cacheApi.getAliasForChatRoom(userId, chatRoomId))
                .doOnError(Crashlytics::logException);
    }

    //******************************************************
    //                ChatRooms
    //******************************************************

    public Observable<ChatRoom> fetchChatRoom(String chatRoomId) {
        return parseApi.findChatRoom(chatRoomId).flatMap(cacheApi::persist)
                .doOnError(Crashlytics::logException);
    }

    public Observable<ChatRoom> getChatRoom(String chatRoomId) {
        return cacheApi.getChatRoom(chatRoomId)
                .onExceptionResumeNext(fetchChatRoom(chatRoomId))
                .doOnError(Crashlytics::logException);
    }

    public Observable<Alias> joinGroupChatRoom() {
        Observable<Alias> observable = getCurrentUser().map(User::objectId)
                .flatMap(parseApi::joinGroupChatRoom)
                .flatMap(cacheApi::persist)
                .flatMap(alias -> Observable.defer(() ->
                        // Cache ChatRoom after joining.
                        getChatRoom(alias.chatRoomId())
                                .flatMap(cacheApi::persist)
                                .map(chatRoom -> alias)));

        return observable.doOnError(Crashlytics::logException);
    }

    public Observable<Alias> leaveChatRoom(String aliasId) {
        return parseApi.leaveChatRoom(aliasId)
                .flatMap(cacheApi::persist)
                .doOnError(Crashlytics::logException);
    }

    /**
     * Get the list of active Aliases for the current user.
     */
    public Observable<List<Alias>> getActiveAliases(String chatRoomId) {
        return parseApi.getActiveAliases(chatRoomId)
                .flatMap(cacheApi::persistAliases)
                .doOnError(Crashlytics::logException);
    }

    /**
     * Get the list of ChatRooms a User is in along with the associated
     * Alias and last ChatEvent sent to that ChatRoom
     */
    public Observable<List<ChatRoomInfo>> getChatRoomInfos() {
        return getCurrentUser().map(User::objectId)
                .flatMap(userId -> Observable.concat(
                        cacheApi.getChatRoomInfos(userId)
                                .compose(sortChatRoomInfoList())
                                .doOnNext(infos -> Timber.v("cached: " + infos.size()))
                                .onExceptionResumeNext(Observable.empty()),
                        // Network call.
                        fetchChatRoomInfos()))
                .doOnError(Crashlytics::logException);
    }

    /**
     * Get the list of ChatRooms a User is in along with the associated
     * Alias and last ChatEvent sent to that ChatRoom from the network
     */
    public Observable<List<ChatRoomInfo>> fetchChatRoomInfos() {
        return getCurrentUser().map(User::objectId)
                .flatMap(userId -> parseApi.getChatRoomInfos(userId)
                        .doOnNext(infos -> cacheApi.persistChatRoomInfosForUserExclusive(userId, infos)))
                .doOnNext(infos -> Timber.v("network: " + infos.size()))
                .compose(sortChatRoomInfoList())
                .doOnError(Crashlytics::logException);
    }

    private Observable.Transformer<List<ChatRoomInfo>, List<ChatRoomInfo>> sortChatRoomInfoList() {
        return o -> o.flatMap(Observable::from)
                .toSortedList((i1, i2) -> i2.chatEvent().updatedAt().compareTo(i1.chatEvent().updatedAt()));
    }

    /**
     * Change the name of a ChatRoom.
     */
    public Observable<ChatRoom> updateChatRoomName(String aliasId, String name) {
        return parseApi.updateChatRoomName(aliasId, name.trim().replace("\n", ""))
                .flatMap(cacheApi::persist)
                .doOnError(Crashlytics::logException);
    }

    /**
     * Sends a ChatEvent to the network. Create new Messages
     * using ChatEvent.createPlaceholderMessage.
     */
    public Observable<ChatEvent> sendMessage(ChatEvent chatEvent) {
        return parseApi.sendMessage(chatEvent.alias().objectId(), chatEvent.body(), chatEvent.objectId())
                .flatMap(cacheApi::persist)
                .doOnError(Crashlytics::logException);
    }

    //******************************************************
    //                ChatEvents
    //******************************************************

    public Observable<ChatEvent> getChatEventStream(String aliasId) {
        return getAlias(aliasId).map(Alias::chatRoomId)
                .flatMap(messageApi::subscribe)
                .filter(o -> o instanceof MessageApiChatEventHolder)
                .cast(MessageApiChatEventHolder.class)
                .map(o -> o.chatEvent)
                .flatMap(cacheApi::persist)
                .doOnError(Crashlytics::logException);
    }

    public Observable<Void> endChatEventStream(String aliasId) {
        return getAlias(aliasId)
                .map(Alias::chatRoomId)
                .flatMap(messageApi::unsubscribe)
                .doOnError(Crashlytics::logException);
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
                        .flatMap(cacheApi::persistChatEvents))
                .doOnError(Crashlytics::logException);
    }

    private Observable.Transformer<List<ChatEvent>, List<ChatEvent>> sortChatEventList() {
        return o -> o.flatMap(Observable::from)
                .toSortedList((ce1, ce2) -> ce2.updatedAt().compareTo(ce1.updatedAt()))
                .doOnError(Crashlytics::logException);
    }

    /**
     * Retrieve past ChatEvents that exist within a specific time window.
     */
    public Observable<List<ChatEvent>> replayChatEvents(String aliasId, Date start, Date end) {
        return parseApi.getChatEventsInRange(aliasId, start, end)
                .flatMap(cacheApi::persistChatEvents)
                .doOnError(Crashlytics::logException);
    }

    //******************************************************
    //                Feedback
    //******************************************************

    /**
     * Send feedback associated with an Alias.
     */
    public Observable<String> sendFeedback(Alias alias, String feedback) {
        return parseApi.sendFeedback(alias, feedback)
                .doOnError(Crashlytics::logException);
    }

    /**
     * Send feedback associated with the current User.
     */
    public Observable<String> sendFeedback(String feedback) {
        return getCurrentUserId().flatMap(userId -> parseApi.sendFeedback(userId, feedback))
                .doOnError(Crashlytics::logException);
    }

    //******************************************************
    //                Miscellaneous
    //******************************************************

    /**
     * Do we need to force an upgrade?
     */
    public Observable<Boolean> checkVersionUpgrade() {
        return parseApi.getMinimumBuildNumber()
                .map(minBuildNumber -> true)//BuildConfig.VERSION_CODE < minBuildNumber)
                .doOnError(Crashlytics::logException);
    }

    /**
     * Send feedback that a user has requested their school to be supported.
     */
    public Observable<String> registerDifferentSchool(String schoolName, String email) {
        return parseApi.sendChangeSchoolRequest(schoolName, email)
                .doOnError(Crashlytics::logException);
    }

    public Observable<String> resetPassword(String email) {
        return parseApi.resetPassword(email);
    }

    /**
     * Thrown when a method that requires a logged in user is called and there is none.
     */
    public static class NoCurrentUserException extends Throwable {
    }
}

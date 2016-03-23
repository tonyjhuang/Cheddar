package com.tonyjhuang.cheddar.ui.chat;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetricTracker;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.background.CheddarGcmListenerService;
import com.tonyjhuang.cheddar.background.PushRegistrationIntentService_;
import com.tonyjhuang.cheddar.background.UnreadMessagesCounter;
import com.tonyjhuang.cheddar.presenter.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.subjects.AsyncSubject;
import rx.subjects.ReplaySubject;

/**
 * Created by tonyjhuang on 3/17/16.
 */
@EBean
public class ChatRoomPresenterImpl implements ChatRoomPresenter {

    private static final String TAG = ChatRoomPresenterImpl.class.getSimpleName();

    // Number of ChatEvents to fetch per replay request.
    private static final int REPLAY_COUNT = 20;

    @Bean
    CheddarApi api;

    @Bean
    UnreadMessagesCounter unreadMessagesCounter;

    /**
     * Caches the current Alias for this room.
     */
    private Subscription aliasSubscription;
    private AsyncSubject<Alias> aliasSubject = AsyncSubject.create();

    /**
     * All new ChatEvents coming in from the backend.
     */
    private Subscription chatEventSubscription;
    private ConnectableObservable<ChatEvent> chatEventObservable;

    /**
     * Caches incoming ChatEvents until the View calls onResume.
     */
    private Subscription cacheChatEventSubscription;
    private ReplaySubject<ChatEvent> cacheChatEventSubject;

    /**
     * Subscription for loading ChatEvent history. Need to keep around to
     * delete in onDestroy.
     */
    private Subscription historyChatEventSubscription;

    /**
     * Caches the result of loading ChatEvent history in case the view has called
     * onPause.
     */
    private Subscription cacheHistoryChatEventSubscription;
    private AsyncSubject<List<ChatEvent>> cacheHistoryChatEventSubject;
    private ChatRoomView view;

    /**
     * BroadcastReceiver for aborting push notification broadcasts.
     */
    private ChatPushBroadcastReceiver chatPushBroadcastReceiver;
    private IntentFilter chatPushIntentFilter;

    /**
     * Keeps track of paging through message history.
     */
    private boolean loadingMessages = false;
    private boolean reachedEndOfMessages = false;

    @Override
    public void setAliasId(String aliasId) {
        chatEventObservable = api.getMessageStream(aliasId).publish();
        chatEventObservable.connect();

        aliasSubscription = api.getAlias(aliasId)
                .compose(Scheduler.backgroundSchedulers())
                .subscribe(aliasSubject);
    }

    @Override
    public void onResume(Context context) {
        aliasSubject.compose(Scheduler.backgroundSchedulers())
                .map(Alias::getChatRoomId)
                .subscribe(chatRoomId -> {
                    unreadMessagesCounter.clear(chatRoomId);
                    PushRegistrationIntentService_.intent(context).registerForPush(chatRoomId).start();
                    registerReceiver(context, chatRoomId);
                });

        if (cacheChatEventSubscription == null) {
            cacheChatEventSubscription = subscribeCacheChatEventSubjectToObservable();
        }

        // Subscribe to cached and future chat events.
        chatEventSubscription = cacheChatEventSubject
                .map(chatEvent -> new ChatEvent[]{chatEvent})
                .map(Arrays::asList)
                .compose(Scheduler.defaultSchedulers())
                .subscribe(this::sendViewNewChatEvents, e -> Log.e(TAG, e.toString()));

        if (cacheHistoryChatEventSubscription != null &&
                !cacheHistoryChatEventSubscription.isUnsubscribed() &&
                (historyChatEventSubscription == null ||
                        historyChatEventSubscription.isUnsubscribed())) {
            // If the View called onPause before the historic ChatEvents loaded,
            // resubscribe to the result of that query here.
            historyChatEventSubscription = subscribeToCacheHistoryChatEventSubject();
        }
    }

    private void registerReceiver(Context context, String chatRoomId) {
        if (chatPushBroadcastReceiver == null) {
            chatPushBroadcastReceiver = new ChatPushBroadcastReceiver(chatRoomId);
            chatPushIntentFilter = new IntentFilter(CheddarGcmListenerService.MESSAGE_EVENT_ACTION);
            chatPushIntentFilter.setPriority(100);
        }
        context.registerReceiver(chatPushBroadcastReceiver, chatPushIntentFilter);
    }

    private void sendViewNewChatEvents(List<ChatEvent> chatEvents) {
        aliasSubject.compose(Scheduler.defaultSchedulers()).subscribe(alias -> {
            if (view != null) view.displayNewChatEvents(alias.getUserId(), chatEvents);
        });
    }

    private Subscription subscribeCacheChatEventSubjectToObservable() {
        cacheChatEventSubject = ReplaySubject.create();
        return chatEventObservable
                .compose(Scheduler.backgroundSchedulers())
                .subscribe(cacheChatEventSubject);
    }

    @Override
    public void onPause(Context context) {
        // Stop sending ChatEvents to View.
        unsubscribe(chatEventSubscription);

        // Restart ReplaySubject to avoid sending ChatEvents that have already
        // been replayed to the View in onResume().
        unsubscribe(cacheChatEventSubscription);
        cacheChatEventSubscription = subscribeCacheChatEventSubjectToObservable();

        // Stop listening to historic ChatEvent loading.
        unsubscribe(historyChatEventSubscription);

        if (chatPushBroadcastReceiver != null) {
            context.unregisterReceiver(chatPushBroadcastReceiver);
        }
    }

    @Override
    public void loadMoreMessages() {
        if (loadingMessages || reachedEndOfMessages) return;
        loadingMessages = true;

        // Subscribe AsyncSubject so the result is cached in case
        // the View calls onPause.
        cacheHistoryChatEventSubject = AsyncSubject.create();
        cacheHistoryChatEventSubscription = aliasSubject.map(Alias::getObjectId)
                .flatMap(aliasId -> api.replayMessageEvents(aliasId, REPLAY_COUNT))
                .compose(Scheduler.backgroundSchedulers())
                .subscribe(cacheHistoryChatEventSubject);

        historyChatEventSubscription = subscribeToCacheHistoryChatEventSubject();
    }

    /**
     * Handles subscribing to the historic ChatEvents loading AsyncSubject.
     */
    private Subscription subscribeToCacheHistoryChatEventSubject() {
        return cacheHistoryChatEventSubject
                .compose(Scheduler.defaultSchedulers())
                .subscribe(chatEvents -> {
                    sendViewOldChatEvents(chatEvents);
                    reachedEndOfMessages = chatEvents.size() < REPLAY_COUNT;
                    new Handler().postDelayed(() -> loadingMessages = false, 250);
                }, e -> {
                    Log.e(TAG, "Failed to load more messages: " + e.toString());
                    view.displayLoadHistoryChatEventsError();
                    loadingMessages = false;
                    reachedEndOfMessages = true;
                });
    }

    private void sendViewOldChatEvents(List<ChatEvent> chatEvents) {
        aliasSubject.compose(Scheduler.defaultSchedulers()).subscribe(alias -> {
            if (view != null) view.displayOldChatEvents(alias.getUserId(), chatEvents);
        });
    }

    @Override
    public void sendMessage(String message) {
        /**
         * TODO: Need synchronous network requests. Sometimes, user tries to send a message before
         * TODO: we're subscribed to the message stream
         */
        aliasSubject.compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    Message placeholder = Message.createPlaceholderMessage(alias, message);
                    view.displayPlaceholderMessage(placeholder);
                    CheddarMetricTracker.trackSendMessage(alias.getChatRoomId(), CheddarMetricTracker.MessageLifecycle.SENT);
                });

        aliasSubject.map(Alias::getObjectId)
                .flatMap(aliasId -> api.sendMessage(aliasId, message))
                .compose(Scheduler.defaultSchedulers())
                .subscribe(aVoid -> trackSentMessage(),
                        throwable -> handleFailedMessage(message));
    }

    private void trackSentMessage() {
        aliasSubject.compose(Scheduler.backgroundSchedulers()).subscribe(alias ->
                CheddarMetricTracker.trackSendMessage(alias.getChatRoomId(),
                        CheddarMetricTracker.MessageLifecycle.DELIVERED));
    }

    private void handleFailedMessage(String message) {
        aliasSubject.compose(Scheduler.defaultSchedulers()).subscribe(alias -> {
            CheddarMetricTracker.trackSendMessage(alias.getChatRoomId(), CheddarMetricTracker.MessageLifecycle.FAILED);
            Message placeholder = Message.createPlaceholderMessage(alias, message);
            if (view != null) {
                view.notifyPlaceholderMessageFailed(placeholder);
            }
        });
    }

    @Override
    public void leaveChatRoom(Context context) {
        api.resetReplayMessageEvents();
        aliasSubject.map(Alias::getObjectId)
                .flatMap(api::leaveChatRoom)
                .compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    PushRegistrationIntentService_.intent(context).unregisterForPush(alias.getChatRoomId());
                    api.endMessageStream(alias.getObjectId()).publish().connect();
                    long lengthOfStay = new Date().getTime() - alias.getCreatedAt().getTime();
                    CheddarMetricTracker.trackLeaveChatRoom(alias.getChatRoomId(), lengthOfStay);
                    view.navigateToListView();
                });
    }

    @Override
    public void sendFeedback(String feedback) {
        aliasSubject.flatMap(alias -> api.sendFeedback(alias.getUserId(), alias.getChatRoomId(), feedback))
                .doOnNext(result -> CheddarMetricTracker.trackFeedback(CheddarMetricTracker.FeedbackLifecycle.SENT))
                .compose(Scheduler.backgroundSchedulers())
                .publish().connect();
    }

    @Override
    public void onDestroy() {
        unsubscribe(aliasSubscription);
        unsubscribe(cacheChatEventSubscription);
        unsubscribe(chatEventSubscription);
        unsubscribe(historyChatEventSubscription);
        unsubscribe(cacheHistoryChatEventSubscription);
        view = null;
    }

    private void unsubscribe(Subscription s) {
        if (s != null) s.unsubscribe();
    }

    @Override
    public void setView(ChatRoomView view) {
        this.view = view;
    }
}
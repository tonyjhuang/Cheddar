package com.tonyjhuang.cheddar.ui.chat;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetrics;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoom;
import com.tonyjhuang.cheddar.background.ConnectivityBroadcastReceiver;
import com.tonyjhuang.cheddar.background.IntentFilters;
import com.tonyjhuang.cheddar.background.UnreadMessagesCounter;
import com.tonyjhuang.cheddar.background.notif.CheddarNotificationService;
import com.tonyjhuang.cheddar.background.notif.PushRegistrationIntentService_;
import com.tonyjhuang.cheddar.utils.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.subjects.AsyncSubject;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;
import timber.log.Timber;

@EBean
public class ChatRoomPresenterImpl implements ChatRoomPresenter {

    // Number of ChatEvents to fetch per replay request.
    private static final int REPLAY_COUNT = 20;

    @Bean
    CheddarApi api;

    @Bean
    UnreadMessagesCounter unreadMessagesCounter;

    @Pref
    CheddarPrefs_ prefs;

    @RootContext
    Context context;

    @Bean
    CheddarNotificationService notificationService;

    /**
     * Current network connection.
     */
    private Subscription networkConnectionSubscription;

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
     * Caches incoming ChatEvents until the View calls onResume. Should
     * subscribe to cacheChatEventSubject for ChatEvents instead of
     * subscribing to chatEventObservable directly.
     */
    private Subscription cacheChatEventSubscription;
    private ReplaySubject<ChatEvent> cacheChatEventSubject;

    /**
     * Subscription for loading ChatEvent history.
     */
    private Subscription historyChatEventSubscription;

    /**
     * Listens on chatEventObservable for new Presence events and retrieves
     * the list of Aliases for the active Chat Room on each event.
     */
    private Subscription activeAliasSubscription;
    private BehaviorSubject<List<Alias>> activeAliasSubject;

    /**
     * Subscription for updating the view with users joining/leaving the Chat Room.
     */
    private Subscription getActiveAliasesSubscription;

    /**
     * Caches the result of loading ChatEvent history in case the view has called
     * onPause.
     */
    private Subscription cacheHistoryChatEventSubscription;
    private BehaviorSubject<List<ChatEvent>> cacheHistoryChatEventSubject;
    private ChatRoomView view;

    /**
     * The stream of ChatRoom name changes.
     */
    private BehaviorSubject<String> chatRoomNameSubject;
    private Subscription cacheChatRoomNameSubscription;
    private Subscription chatRoomNameSubscription;

    /**
     * BroadcastReceiver for aborting push notification broadcasts.
     */
    private ChatPushBroadcastReceiver chatPushBroadcastReceiver;
    private IntentFilter chatPushIntentFilter;

    /**
     * Keeps track of paging through message history.
     */
    private boolean loadingMessages = false;
    private boolean reachedEndOfChatEvents = false;
    private boolean firstLoad = true;

    /**
     * Time that the user's client lost internet connection. When
     * connection is regained, get all messages from the moment
     * of reconnection to this date.
     */
    private Date lostConnection;

    @Override
    public void setAliasId(String aliasId) {
        Timber.d("setAliasId");
        api.resetReplayChatEvents();
        chatEventObservable = api.getMessageStream(aliasId).publish();
        chatEventObservable.connect();

        // Make sure we have an active Alias from the server.
        api.fetchAlias(aliasId).compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    if (!alias.active()) {
                        // Respect server switches to active status.
                        leaveChatRoom();
                    }
                }, error -> onFailedToRetrieveAlias());

        aliasSubscription = api.getAlias(aliasId)
                .compose(Scheduler.backgroundSchedulers())
                .subscribe(aliasSubject);

        networkConnectionSubscription = ConnectivityBroadcastReceiver.connectionObservable
                .map(status -> status == ConnectivityBroadcastReceiver.Status.CONNECTED)
                .compose(Scheduler.defaultSchedulers())
                .subscribe(connected -> {
                    if (view != null) {
                        if (connected) {
                            view.hideNetworkConnectionError();
                        } else {
                            view.displayNetworkConnectionError();
                        }
                    }
                    if (connected) {
                        // If there's no internet connection when the app first starts,
                        // aliasSubject will receive an error due to network timeouts.
                        // Restart the connection here.
                        if (aliasSubject.hasThrowable()) {
                            aliasSubscription.unsubscribe();
                            aliasSubscription = api.fetchAlias(aliasId)
                                    .compose(Scheduler.backgroundSchedulers())
                                    .subscribe(aliasSubject);
                        }

                        if (firstLoad && lostConnection != null) {
                            // The app was started without internet connection. Network
                            // connection was regained, let's start 'er up!
                            init(context);
                        } else if (!firstLoad && lostConnection != null) {
                            // Grab all the messages that were missed while the user was offline.
                            api.replayChatEvents(aliasId, new Date(), lostConnection)
                                    .compose(Scheduler.defaultSchedulers())
                                    .subscribe(this::displayViewNewChatEvents, error ->
                                            Timber.e("error loading missed messages: " + error));
                        }
                    } else {
                        lostConnection = new Date();
                    }
                });
    }

    @Override
    public void setView(ChatRoomView view) {
        this.view = view;
    }

    @Override
    public void onResume() {
        if (!ConnectivityBroadcastReceiver.isConnected(context)) return;
        Timber.d("onResume");
        init(context);
    }

    private void init(Context context) {
        Timber.d("init");
        aliasSubject.compose(Scheduler.backgroundSchedulers())
                .compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    if (!alias.active()) {
                        // Respect server switches to active status.
                        leaveChatRoom();
                        return;
                    }

                    String chatRoomId = alias.chatRoomId();
                    unreadMessagesCounter.clear(chatRoomId);
                    notificationService.removeNotification(chatRoomId);

                    PushRegistrationIntentService_.intent(context).registerForPush(chatRoomId).start();
                    registerReceiver(context, chatRoomId);

                    api.getChatRoom(chatRoomId).compose(Scheduler.defaultSchedulers())
                            .subscribe(chatRoom -> {
                                if (view != null) view.displayChatRoomName(chatRoom.name());
                            }, error -> Timber.e(error, "couldn't retrieve ChatRoom " + chatRoomId));

                    if (activeAliasSubscription == null || activeAliasSubscription.isUnsubscribed()) {
                        // Listen to ChatEvent stream for Presence events and get
                        // the updated list of active Aliases.
                        Observable<List<Alias>> aliasUpdates = chatEventObservable
                                .filter(chatEvent -> chatEvent.type().equals(ChatEvent.ChatEventType.PRESENCE))
                                .flatMap(chatEvent -> api.getActiveAliases(chatRoomId));

                        activeAliasSubject = BehaviorSubject.create();
                        activeAliasSubscription = api.getActiveAliases(chatRoomId)
                                .concatWith(aliasUpdates)
                                .compose(Scheduler.backgroundSchedulers())
                                .subscribe(activeAliasSubject);
                    }

                    // Update View if number of active Aliases changes.
                    getActiveAliasesSubscription = activeAliasSubject
                            .compose(Scheduler.defaultSchedulers())
                            .subscribe(aliases -> {
                                if (view != null)
                                    view.displayActiveAliases(aliases, alias.userId());
                            }, error -> Timber.e(error, "couldn't get active aliases"));

                    // Listen for CHANGE_ROOM_NAME ChatEvents.
                    if (cacheChatRoomNameSubscription == null || cacheChatRoomNameSubscription.isUnsubscribed()) {
                        chatRoomNameSubject = BehaviorSubject.create();
                        cacheChatRoomNameSubscription =
                                Observable.concat(
                                        // Get the latest ChatRoom name on start.
                                        api.fetchChatRoom(chatRoomId).map(ChatRoom::name)
                                                .compose(Scheduler.backgroundSchedulers()),
                                        chatEventObservable
                                                .compose(Scheduler.backgroundSchedulers())
                                                .filter(chatEvent -> chatEvent.type().equals(ChatEvent.ChatEventType.CHANGE_ROOM_NAME))
                                                .map(ChatEvent::roomName))
                                        .subscribe(chatRoomNameSubject);
                    }

                    // Update View on new CHANGE_ROOM_NAME event.
                    chatRoomNameSubscription = chatRoomNameSubject.compose(Scheduler.defaultSchedulers())
                            .subscribe(name -> {
                                if (view != null) view.displayChatRoomName(name);
                            }, error -> Timber.e(error, "couldn't get chatroom name."));

                }, error -> {
                    Timber.e("couldn't find current alias in onResume! " + error.toString());
                    // This generally happens if the alias was deleted on the backend.
                    onFailedToRetrieveAlias();
                });

        if (cacheChatEventSubscription == null || cacheChatEventSubscription.isUnsubscribed()) {
            cacheChatEventSubscription = subscribeCacheChatEventSubjectToObservable();
        }

        if (chatEventSubscription == null || chatEventSubscription.isUnsubscribed()) {
            // Subscribe to cached and future chat events.
            chatEventSubscription = cacheChatEventSubject
                    .doOnNext(chatEvent -> Timber.i("new chat event: " + chatEvent))
                    .map(chatEvent -> new ChatEvent[]{chatEvent})
                    .map(Arrays::asList)
                    .compose(Scheduler.defaultSchedulers())
                    .subscribe(this::displayViewNewChatEvents, e -> Timber.e(e.toString()));
        }

        if (cacheHistoryChatEventSubscription != null &&
                !cacheHistoryChatEventSubscription.isUnsubscribed() &&
                (historyChatEventSubscription == null ||
                        historyChatEventSubscription.isUnsubscribed())) {
            // If the View called onPause before the historic ChatEvents loaded,
            // resubscribe to the result of that query here.
            historyChatEventSubscription = subscribeToCacheHistoryChatEventSubject();
        }
    }

    /**
     * Updates the view if we couldn't get this ChatRoom's Alias.
     */
    private void onFailedToRetrieveAlias() {
        if (view != null) {
            view.navigateToListView();
            // TODO:How do we unregister for push notifications here?
            //unregisterForPush(context, alias.getChatRoomId())
        }
    }

    private void registerReceiver(Context context, String chatRoomId) {
        if (chatPushBroadcastReceiver == null) {
            chatPushBroadcastReceiver = new ChatPushBroadcastReceiver(chatRoomId);
            chatPushIntentFilter = IntentFilters.chatEventIntentFilter(100);
        }
        context.registerReceiver(chatPushBroadcastReceiver, chatPushIntentFilter);
    }

    private void displayViewNewChatEvents(List<ChatEvent> chatEvents) {
        aliasSubject.compose(Scheduler.defaultSchedulers()).subscribe(alias -> {
            if (view != null)
                view.displayNewChatEvents(alias.objectId(), chatEvents);
        }, error -> onFailedToRetrieveAlias());
    }

    /**
     * cacheChatEventSubject will subscribe to our ChatEvent message stream and
     * store all incoming ChatEvents to an internal cache. When a new subscriber
     * subscribes to cacheChatEventSubject, it will immediately receive all
     * incoming ChatEvents from the moment cacheChatEventSubject was first created
     * in order as well as all future ChatEvents as they come in.
     */
    private Subscription subscribeCacheChatEventSubjectToObservable() {
        cacheChatEventSubject = ReplaySubject.create();
        return chatEventObservable
                .compose(Scheduler.backgroundSchedulers())
                .subscribe(cacheChatEventSubject);
    }

    @Override
    public void onPause() {
        // Stop sending ChatEvents to View.
        unsubscribe(chatEventSubscription);

        // Stop sending user join/leave events to View.
        unsubscribe(getActiveAliasesSubscription);

        // Stop listening for ChatRoom name changes.
        unsubscribe(chatRoomNameSubscription);

        // Restart ReplaySubject to avoid sending ChatEvents that have already
        // been replayed to the View in onResume().
        unsubscribe(cacheChatEventSubscription);
        cacheChatEventSubscription = subscribeCacheChatEventSubjectToObservable();

        // Stop listening to historic ChatEvent loading.
        unsubscribe(historyChatEventSubscription);

        if (chatPushBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(chatPushBroadcastReceiver);
            } catch (IllegalArgumentException e) {
                Timber.e("Receiver not registered?? " + e.toString());
            }
        }
    }

    @Override
    public void loadMoreMessages() {
        if (!ConnectivityBroadcastReceiver.isConnected(context)) return;

        if (loadingMessages || reachedEndOfChatEvents) return;
        Timber.d("loading messages..");
        loadingMessages = true;
        firstLoad = false;

        cacheHistoryChatEventSubject = BehaviorSubject.create();
        cacheHistoryChatEventSubscription = aliasSubject.map(Alias::objectId)
                .flatMap(aliasId -> api.replayChatEvents(aliasId, REPLAY_COUNT))
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
                .doAfterTerminate(() -> loadingMessages = false)
                .subscribe(chatEvents -> {
                    sendViewOldChatEvents(chatEvents);
                    if (chatEvents.size() < REPLAY_COUNT) {
                        reachedEndOfChatEvents = true;
                        if (view != null) view.notifyEndOfChatEvents();
                    } else {
                        reachedEndOfChatEvents = false;
                    }
                }, e -> {
                    Timber.e(e, "Failed to load more messages");
                    view.displayLoadHistoryChatEventsError();
                    loadingMessages = false;
                    reachedEndOfChatEvents = true;
                });
    }

    private void sendViewOldChatEvents(List<ChatEvent> chatEvents) {
        aliasSubject.compose(Scheduler.defaultSchedulers()).subscribe(alias -> {
            if (view != null) view.displayOldChatEvents(alias.userId(), chatEvents);
        }, error -> onFailedToRetrieveAlias());
    }

    @Override
    public void sendMessage(String body) {
        aliasSubject.compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    ChatEvent placeholder = ChatEvent.createPlaceholderMessage(alias, body);
                    if (view != null) view.displayPlaceholderMessage(placeholder);
                    api.sendMessage(placeholder).compose(Scheduler.defaultSchedulers())
                            .subscribe(chatEvent -> {
                                if (view != null) view.displayNewChatEvents(alias.userId(),
                                        Collections.singletonList(placeholder));
                                CheddarMetrics.trackSendMessage(placeholder.alias().chatRoomId(),
                                        CheddarMetrics.MessageLifecycle.SENT);
                            }, error -> {
                                Timber.e("Send message failed! " + error);
                                if (view != null) view.notifyPlaceholderMessageFailed(placeholder);
                                CheddarMetrics.trackSendMessage(placeholder.alias().chatRoomId(),
                                        CheddarMetrics.MessageLifecycle.FAILED);
                            });
                }, error -> {
                    Timber.e("couldn't get current alias while sending message");
                    onFailedToRetrieveAlias();
                });
    }

    @Override
    public void leaveChatRoom() {
        unsubscribe(activeAliasSubscription);
        unsubscribe(getActiveAliasesSubscription);
        unsubscribe(chatEventSubscription);
        unsubscribe(cacheChatEventSubscription);
        unsubscribe(cacheChatRoomNameSubscription);
        unsubscribe(chatRoomNameSubscription);
        unsubscribe(historyChatEventSubscription);
        api.resetReplayChatEvents();
        aliasSubject.compose(Scheduler.backgroundSchedulers())
                .doOnNext(alias -> unregisterForPush(context, alias.chatRoomId()))
                .map(Alias::objectId)
                .flatMap(api::leaveChatRoom)
                .compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    api.endMessageStream(alias.objectId()).publish().connect();
                    long lengthOfStay = new Date().getTime() - alias.createdAt().getTime();
                    CheddarMetrics.trackLeaveChatRoom(alias.chatRoomId(), lengthOfStay);
                    view.navigateToListView();
                }, error -> {
                    Timber.e("uhh");
                    Timber.e("couldn't find current alias to leave chatroom! " + error.toString());
                    onFailedToRetrieveAlias();
                });
    }

    @Override
    public void updateChatRoomName(String name) {
        aliasSubject.map(Alias::objectId)
                .flatMap(aliasId -> api.updateChatRoomName(aliasId, name))
                .compose(Scheduler.defaultSchedulers())
                .map(ChatRoom::name)
                .subscribe(chatRoomName -> {
                    if (view != null) view.displayChatRoomName(chatRoomName);
                }, error -> {
                    Timber.e(error, "failed to update chatroom name");
                    if (view != null) view.displayChatRoomNameChangeError();
                });
    }

    private void unregisterForPush(Context context, String chatRoomId) {
        PushRegistrationIntentService_.intent(context).unregisterForPush(chatRoomId).start();
    }


    @Override
    public void sendFeedback(String name, String feedback) {
        try {
            String versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            aliasSubject.flatMap(alias -> api.sendFeedback(versionName, alias, name, feedback))
                    .doOnNext(result -> CheddarMetrics.trackFeedback(CheddarMetrics.FeedbackLifecycle.SENT))
                    .compose(Scheduler.backgroundSchedulers())
                    .publish().connect();
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e("couldn't get versionName: " + e);
        }
    }

    @Override
    public void onDestroy() {
        aliasSubject.compose(Scheduler.backgroundSchedulers())
                .map(Alias::objectId)
                .flatMap(api::endMessageStream)
                .doOnNext(result -> Timber.d("ended message stream"))
                .doOnError(error -> Timber.e(error, "uh oh"))
                .publish().connect();
        unsubscribe(networkConnectionSubscription);
        unsubscribe(aliasSubscription);
        unsubscribe(cacheChatEventSubscription);
        unsubscribe(chatEventSubscription);
        unsubscribe(historyChatEventSubscription);
        unsubscribe(cacheHistoryChatEventSubscription);
        unsubscribe(activeAliasSubscription);
        unsubscribe(getActiveAliasesSubscription);
        unsubscribe(chatRoomNameSubscription);
        unsubscribe(cacheChatRoomNameSubscription);
        view = null;
    }

    private void unsubscribe(Subscription s) {
        if (s != null) s.unsubscribe();
    }
}

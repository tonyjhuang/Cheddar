package com.tonyjhuang.cheddar.ui.list;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.util.Pair;

import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetrics;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.api.network.ParseApi;
import com.tonyjhuang.cheddar.background.IntentFilters;
import com.tonyjhuang.cheddar.background.notif.PushRegistrationIntentService_;
import com.tonyjhuang.cheddar.utils.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

@EBean
public class ChatRoomListPresenterImpl implements ChatRoomListPresenter {

    @RootContext
    Context context;

    @Bean
    CheddarApi api;

    @Bean
    ParseApi pApi;

    @Pref
    CheddarPrefs_ prefs;
    /**
     * BroadcastReceiver that listens for incoming push notifications.
     */
    @Bean
    ChatRoomListPushBroadcastReceiver pushReceiver;
    IntentFilter pushReceiverIntentFilter = IntentFilters.chatEventIntentFilter(100);
    /**
     * Subscription to refreshChatList.
     */
    private Subscription chatRoomSubscription;
    /**
     * Subscription to the push notification BroadcastReceiver.
     */
    private Subscription pushSubscription;
    /**
     * That view that this presenter is presenting with.
     */
    private ChatRoomListView view;

    @Override
    public void setView(ChatRoomListView view) {
        this.view = view;
        if (BuildConfig.DEBUG) {
            api.getCurrentUser().compose(Scheduler.defaultSchedulers())
                    .map(User::username)
                    .subscribe(username -> {
                        if (view != null) view.displayUserEmail(username);
                    }, error -> Timber.e(error, "couldnt get current user"));
        }
        api.getCurrentUser().map(User::objectId)
                .flatMap(api::fetchUser)
                .compose(Scheduler.backgroundSchedulers())
                .doOnError(error -> {
                    Timber.e(error, "failed to get current user from network.");
                    logout();
                }).publish().connect();
    }

    @Override
    public void onResume() {
        unsubscribe(chatRoomSubscription);
        chatRoomSubscription = refreshChatList().publish().connect();
        context.registerReceiver(pushReceiver, pushReceiverIntentFilter);
        pushSubscription = pushReceiver.getChatEvents()
                .compose(Scheduler.backgroundSchedulers())
                .subscribe(chatEvent -> chatRoomSubscription = refreshChatList().publish().connect(),
                        error -> Timber.e(error, "huh?"));

    }

    /**
     * Fetch the list of ChatRoomInfos.
     */
    private Observable<Pair<User, List<ChatRoomInfo>>> refreshChatList() {
        return Observable.combineLatest(
                api.getCurrentUser(),
                api.getChatRoomInfos().doOnNext(infoList -> {
                    List<String> chatRoomIds = new ArrayList<>();
                    for (ChatRoomInfo info : infoList) {
                        chatRoomIds.add(info.chatRoom().objectId());
                    }
                    PushRegistrationIntentService_.intent(context).registerAll(chatRoomIds).start();
                }), Pair::new)
                .compose(Scheduler.defaultSchedulers())
                .doOnNext(result -> {
                    if (view != null) view.displayList(result.second, result.first.objectId());
                }).doOnError(error -> {
                    Timber.e(error, "couldn't get list");
                    if (view != null) view.showGetListError();
                });
    }

    @Override
    public void onJoinChatRoomClicked() {
        api.joinGroupChatRoom().compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    CheddarMetrics.trackJoinChatRoom(alias.chatRoomId());
                    unsubscribe(chatRoomSubscription);
                    chatRoomSubscription = refreshChatList().subscribe(result -> {
                        if (view != null) {
                            view.displayList(result.second, result.first.objectId());
                            view.navigateToChatView(alias.objectId());
                        }
                    }, error -> {
                        Timber.e(error, "Couldn't join chat room");
                        if (view != null) view.showJoinChatError();
                    });
                });
    }

    @Override
    public void logout() {
        Observable<List<String>> unregisterFromPush = api.getCurrentUser().map(User::objectId)
                .flatMap(api::fetchChatRoomInfos)
                .flatMap(Observable::from)
                .map(i -> i.chatRoom().objectId())
                .toList()
                .doOnNext(ids -> PushRegistrationIntentService_.intent(context).unregisterAll(ids).start())
                .compose(Scheduler.backgroundSchedulers());

        unregisterFromPush
                .onExceptionResumeNext(Observable.just(null))
                .flatMap(result -> api.logoutCurrentUser())
                .compose(Scheduler.defaultSchedulers())
                .subscribe(result -> {
                    CheddarMetrics.trackLogout();
                    if (view != null) view.navigateToSignUpView();
                }, error -> {
                    if (view != null) view.showLogoutError();
                    Timber.e(error, "failed to logout");
                });
    }

    @Override
    public void sendFeedback(String feedback) {
        api.sendFeedback(feedback)
                .doOnNext(result -> CheddarMetrics.trackFeedback(CheddarMetrics.FeedbackLifecycle.SENT))
                .compose(Scheduler.backgroundSchedulers())
                .publish().connect();
    }

    @Override
    public void onPause() {
        unsubscribe(chatRoomSubscription);
        unsubscribe(pushSubscription);
        context.unregisterReceiver(pushReceiver);
    }

    @Override
    public void onDestroy() {
        unsubscribe(chatRoomSubscription);
        view = null;
    }

    @Override
    public void debugReset() {
        api.debugReset().subscribe(aVoid -> {
            if (view != null) view.navigateToSignUpView();
        }, error -> Timber.e(error, "couldn't reset?"));
    }

    private void unsubscribe(Subscription s) {
        if (s != null) s.unsubscribe();
    }
}

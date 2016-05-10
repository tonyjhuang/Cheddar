package com.tonyjhuang.cheddar.ui.list;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.util.Pair;

import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.api.CheddarApi;
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
import rx.functions.Action1;
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

    private Action1<List<ChatRoomInfo>> registerForPush() {
        return infoList -> {
            List<String> chatRoomIds = new ArrayList<>();
            for (ChatRoomInfo info : infoList) {
                chatRoomIds.add(info.chatRoom().objectId());
            }
            PushRegistrationIntentService_.intent(context).registerAll(chatRoomIds);
        };
    }

    /**
     * Fetch the list of ChatRoomInfos.
     */
    private Observable<Pair<User, List<ChatRoomInfo>>> refreshChatList() {
        return Observable.combineLatest(
                api.getCurrentUser(),
                api.getChatRooms().doOnNext(registerForPush()),
                Pair::new)
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

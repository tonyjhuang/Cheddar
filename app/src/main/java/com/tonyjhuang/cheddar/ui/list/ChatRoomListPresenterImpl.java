package com.tonyjhuang.cheddar.ui.list;

import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.network.ParseApi;
import com.tonyjhuang.cheddar.ui.chat.ChatRoomPresenterImpl;
import com.tonyjhuang.cheddar.utils.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

/**
 * Created by tonyjhuang on 4/14/16.
 */
@EBean
public class ChatRoomListPresenterImpl implements ChatRoomListPresenter {

    @Bean
    CheddarApi api;

    @Bean
    ParseApi pApi;

    /**
     * The view's subscription to chatRoomSubject.
     */
    private Subscription chatRoomSubscription;

    /**
     * That view that this presenter is presenting with.
     */
    private ChatRoomListView view;

    public ChatRoomListPresenterImpl() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void setView(ChatRoomListView view) {
        this.view = view;
    }

    @Override
    public void onResume() {
        unsubscribe(chatRoomSubscription);
        chatRoomSubscription = refreshChatList().publish().connect();
    }

    /**
     * Fetch the list of ChatRoomInfos.
     */
    private Observable<List<ChatRoomInfo>> refreshChatList() {
        // Only subscribe if the list hasn't been loaded yet.
        return api.getChatRooms().compose(Scheduler.backgroundSchedulers())
                .flatMap(Observable::from)
                .toSortedList((i1, i2) -> i2.chatEvent().updatedAt().compareTo(i1.chatEvent().updatedAt()))
                .compose(Scheduler.defaultSchedulers())
                .doOnNext(infos -> {
                    Timber.i(infos.toString());
                    if (view != null) view.displayList(infos);
                }).doOnError(error -> {
                    Timber.e("couldn't get list: " + error);
                    if (view != null) view.showGetListError();
                });
    }

    @Subscribe
    public void onLeaveChatRoomEvent(ChatRoomPresenterImpl.LeaveChatRoomEvent event) {
        unsubscribe(chatRoomSubscription);
        chatRoomSubscription = refreshChatList().publish().connect();
    }

    @Override
    public void onJoinChatRoomClicked() {
        api.joinGroupChatRoom().compose(Scheduler.defaultSchedulers())
                .doOnError(error -> Timber.e("uhoh: " + error))
                .subscribe(alias -> {
                    unsubscribe(chatRoomSubscription);
                    chatRoomSubscription = refreshChatList().subscribe(infos -> {
                        if (view != null) {
                            view.displayList(infos);
                            view.navigateToChatView(alias.objectId());
                        }
                    }, error -> {
                        if (view != null) view.showJoinChatError();
                    });
                });
    }

    @Override
    public void onPause() {
        unsubscribe(chatRoomSubscription);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        unsubscribe(chatRoomSubscription);
        view = null;
    }


    private void unsubscribe(Subscription s) {
        if (s != null) s.unsubscribe();
    }
}

package com.tonyjhuang.cheddar.ui.list;

import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.network.ParseApi;
import com.tonyjhuang.cheddar.presenter.Scheduler;
import com.tonyjhuang.cheddar.ui.chat.ChatRoomPresenterImpl;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.subjects.AsyncSubject;
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
     * Caches the result of the getChatRooms api call.
     */
    private Subscription cachedChatRoomSubscription;
    private AsyncSubject<List<ChatRoomInfo>> chatRoomSubject = AsyncSubject.create();

    /**
     * The view's subscription to chatRoomSubject.
     */
    private Subscription chatRoomSubscription;

    /**
     * That view that this presenter is presenting with.
     */
    private ChatRoomListView view;

    /**
     * Has the list of chat rooms not been loaded yet?
     */
    private boolean firstLoad = true;

    public ChatRoomListPresenterImpl() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void setView(ChatRoomListView view) {
        this.view = view;
    }

    @Override
    public void onResume() {
        subscribeCachedChatRoomSubject();

        if (firstLoad) {
            // Only subscribe if the list hasn't been loaded yet.
            chatRoomSubscription = fetchChatRoomInfoList().subscribe(infos -> {
                if (view != null) view.displayList(infos);
            }, error -> {
                if (view != null) view.showGetListError();
            });
        }
    }

    /**
     * Fetch the list of ChatRoomInfos.
     */
    private Observable<List<ChatRoomInfo>> fetchChatRoomInfoList() {
        // Only subscribe if the list hasn't been loaded yet.
        return chatRoomSubject.compose(Scheduler.defaultSchedulers())
                .doOnNext(infos -> firstLoad = false)
                .doOnNext(infos -> Timber.i(infos.toString()))
                .doOnError(error -> Timber.e(error.toString()));
    }

    /**
     * Subscribe chatRoomSubject to fetch a new list of ChatRoomInfos if it's not
     * subscribed already.
     */
    private void subscribeCachedChatRoomSubject() {
        if (cachedChatRoomSubscription == null || cachedChatRoomSubscription.isUnsubscribed()) {
            chatRoomSubject = AsyncSubject.create();
            cachedChatRoomSubscription = api.getChatRooms()
                    .compose(Scheduler.backgroundSchedulers())
                    .flatMap(Observable::from)
                    .toSortedList((i1, i2) -> i2.chatEvent().updatedAt().compareTo(i1.chatEvent().updatedAt()))
                    .compose(Scheduler.backgroundSchedulers())
                    .doOnError(error -> Timber.e(error.toString()))
                    .subscribe(chatRoomSubject);
        }
    }

    @Subscribe
    public void onLeaveChatRoomEvent(ChatRoomPresenterImpl.LeaveChatRoomEvent event) {
        Timber.d("onleave");
        if (view != null) view.removeChatRoom(event.chatRoomId);
    }

    @Override
    public void onJoinChatRoomClicked() {
        api.joinGroupChatRoom().compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    unsubscribe(cachedChatRoomSubscription);
                    firstLoad = false;
                    subscribeCachedChatRoomSubject();
                    chatRoomSubscription = fetchChatRoomInfoList()
                            .doOnNext(infos -> Timber.i("new infos: " + infos))
                            .subscribe(infos -> {
                                if (view != null) {
                                    view.displayList(infos);
                                    view.navigateToChatView(alias.objectId());
                                }
                            }, error -> {
                                if (view != null) view.showJoinChatError();
                            });
                }, error -> {
                    if (view != null) view.showJoinChatError();
                    Timber.e(error.toString());
                });
    }

    @Override
    public void onPause() {
        unsubscribe(chatRoomSubscription);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        view = null;
        unsubscribe(cachedChatRoomSubscription);
        unsubscribe(chatRoomSubscription);
    }


    private void unsubscribe(Subscription s) {
        if (s != null) s.unsubscribe();
    }
}

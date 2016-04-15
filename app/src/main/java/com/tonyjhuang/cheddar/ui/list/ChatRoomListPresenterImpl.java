package com.tonyjhuang.cheddar.ui.list;

import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.ChatRoomInfo;
import com.tonyjhuang.cheddar.presenter.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Func2;
import rx.subjects.AsyncSubject;
import timber.log.Timber;

/**
 * Created by tonyjhuang on 4/14/16.
 */
@EBean
public class ChatRoomListPresenterImpl implements ChatRoomListPresenter {

    @Bean
    CheddarApi api;

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

    @Override
    public void setView(ChatRoomListView view) {
        this.view = view;
    }

    @Override
    public void onResume() {
        if (cachedChatRoomSubscription == null || cachedChatRoomSubscription.isUnsubscribed()) {
            cachedChatRoomSubscription = api.getChatRooms()
                    .compose(Scheduler.backgroundSchedulers())
                    .flatMap(Observable::from)
                    .toSortedList((i1, i2) -> i2.chatEvent.getUpdatedAt().compareTo(i1.chatEvent.getUpdatedAt()))
                    .compose(Scheduler.backgroundSchedulers())
                    .doOnNext(i -> Timber.i(i.toString()))
                    .doOnError(error -> Timber.e(error.toString()))
                    .subscribe(chatRoomSubject);
        }
        if (firstLoad) {
            // Only subscribe if the list hasn't been loaded yet.
            chatRoomSubscription = chatRoomSubject.compose(Scheduler.defaultSchedulers())
                    .subscribe(infos -> {
                        if (view != null) {
                            firstLoad = false;
                            view.displayList(infos);
                        }
                    }, error -> Timber.e(error.toString()));
        }
    }

    @Override
    public void onJoinChatRoomClicked() {
        api.joinNextAvailableGroupChatRoom().compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    if (view != null) view.navigateToChatView(alias.getObjectId());
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
        view = null;
        unsubscribe(cachedChatRoomSubscription);
        unsubscribe(chatRoomSubscription);
    }


    private void unsubscribe(Subscription s) {
        if (s != null) s.unsubscribe();
    }
}

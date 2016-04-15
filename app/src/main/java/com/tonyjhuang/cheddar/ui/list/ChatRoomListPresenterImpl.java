package com.tonyjhuang.cheddar.ui.list;

import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.ChatRoomInfo;
import com.tonyjhuang.cheddar.presenter.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

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

    @Override
    public void setView(ChatRoomListView view) {
        this.view = view;
    }

    @Override
    public void onResume() {
        if (cachedChatRoomSubscription == null || cachedChatRoomSubscription.isUnsubscribed()) {
            cachedChatRoomSubscription = api.getChatRooms()
                    .compose(Scheduler.backgroundSchedulers())
                    .doOnNext(i -> Timber.i(i.toString()))
                    .doOnError(error -> Timber.e(error.toString()))
                    .subscribe(chatRoomSubject);
        }
        chatRoomSubscription = chatRoomSubject.compose(Scheduler.defaultSchedulers())
                .subscribe(infos -> {
                            if (view != null) view.displayList(infos);
                        }, error -> Timber.e(error.toString()));
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

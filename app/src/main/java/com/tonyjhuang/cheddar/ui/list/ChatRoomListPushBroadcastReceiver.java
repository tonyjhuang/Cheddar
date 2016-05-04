package com.tonyjhuang.cheddar.ui.list;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

import org.androidannotations.annotations.EBean;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

/**
 * Pushes ChatEvents that come in via push to a subscriber.
 */
@EBean
public class ChatRoomListPushBroadcastReceiver extends BroadcastReceiver {

    private Subscriber<? super ChatEvent> subscriber;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("chatEvent")) {
            ChatEvent chatEvent = intent.getParcelableExtra("chatEvent");
            if(subscriber != null && !subscriber.isUnsubscribed()) {
                subscriber.onNext(chatEvent);
            }
        }
    }

    public Observable<ChatEvent> getChatEvents() {
        return Observable.create(subscriber -> this.subscriber = subscriber);
    }
}

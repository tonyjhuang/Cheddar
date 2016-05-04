package com.tonyjhuang.cheddar.background.notif;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tonyjhuang.cheddar.api.cache.CacheApi;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;

import timber.log.Timber;

/**
 * Catches ChatEvents that come in by push notification and caches them.
 */
@EReceiver
public class CacheChatEventBroadcastReceiver extends BroadcastReceiver{
    @Bean
    CacheApi cacheApi;

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive!");
        if(intent.hasExtra("chatEvent")) {
            ChatEvent chatEvent = intent.getParcelableExtra("chatEvent");
            cacheApi.forcePersist(chatEvent);
            cacheApi.getMostRecentChatEventForChatRoom(chatEvent.alias().chatRoomId())
                    .subscribe(ce -> Timber.i("ce: " + ce));
        }
    }

}

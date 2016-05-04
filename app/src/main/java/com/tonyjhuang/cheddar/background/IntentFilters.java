package com.tonyjhuang.cheddar.background;

import android.content.IntentFilter;

import com.tonyjhuang.cheddar.background.notif.CheddarGcmListenerService;

/**
 * Created by tonyjhuang on 5/4/16.
 */
public class IntentFilters {
    public static IntentFilter chatEventIntentFilter(int priority) {
        IntentFilter chatPushIntentFilter = new IntentFilter(CheddarGcmListenerService.CHAT_EVENT_ACTION);
        chatPushIntentFilter.setPriority(priority);
        return chatPushIntentFilter;
    }
}

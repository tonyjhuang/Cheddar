package com.tonyjhuang.cheddar.background.notif;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by tonyjhuang on 3/1/16.
 */
public class CheddarInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        PushRegistrationIntentService_.intent(this).onTokenRefresh().start();
    }
}

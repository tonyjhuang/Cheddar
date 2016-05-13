package com.tonyjhuang.cheddar.background.notif;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Listens for InstanceID expiry events.
 */
public class CheddarInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        PushRegistrationIntentService_.intent(this).onTokenRefresh().start();
    }
}

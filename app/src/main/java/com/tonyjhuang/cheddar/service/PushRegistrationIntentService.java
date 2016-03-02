package com.tonyjhuang.cheddar.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.api.support.app.AbstractIntentService;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjhuang on 3/1/16.
 */
@EIntentService
public class PushRegistrationIntentService extends AbstractIntentService {

    private static final String TAG = PushRegistrationIntentService.class.getSimpleName();

    //
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Pref
    CheddarPrefs_ prefs;

    public PushRegistrationIntentService() {
        super(TAG);
    }

    @ServiceAction
    void getRegistrationToken() {
        String token = prefs.gcmRegistrationToken().get();
        if (token == null) {
            registerForPush();
        } else {
            postEvent(new RegistrationCompletedEvent(token));
        }
    }

    @ServiceAction
    void registerForPush() {
        prefs.gcmRegistrationToken().put(null);
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.i(TAG, "token: " + token);

            prefs.gcmRegistrationToken().put(token);
            postEvent(new RegistrationCompletedEvent(token));
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            postEvent(new RegistrationCompletedEvent(null));
        }
    }

    private void postEvent(Object event) {
        handler.post(() -> EventBus.getDefault().post(event));
    }

    public static class RegistrationCompletedEvent {
        public String token;

        public RegistrationCompletedEvent(String token) {
            this.token = token;
        }
    }
}

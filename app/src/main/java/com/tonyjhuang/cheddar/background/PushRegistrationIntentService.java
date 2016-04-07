package com.tonyjhuang.cheddar.background;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.MessageApi;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.api.support.app.AbstractIntentService;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tonyjhuang on 3/1/16.
 */
@EIntentService
public class PushRegistrationIntentService extends AbstractIntentService {

    private static final String TAG = PushRegistrationIntentService.class.getSimpleName();
    private static final Gson gson = new Gson();

    @Bean
    MessageApi messageApi;

    @Pref
    CheddarPrefs_ prefs;

    public PushRegistrationIntentService() {
        super(TAG);
    }

    @ServiceAction
    void registerForPush(String channel) {
        String token = getToken();
        if (token == null) {
            showRegistrationFailedError();
        } else {
            addRegisteredChannel(channel);
            messageApi.registerForPushNotifications(channel, token).publish().connect();
        }
    }

    private void showRegistrationFailedError() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new DisplayToast(this, getString(R.string.gcm_registration_failed)));
    }

    @ServiceAction
    void unregisterForPush(String channel) {
        String token = getToken();
        if (token == null) {
            Log.e(TAG, "COULDN'T RETRIEVE GCM TOKEN");
            showUnregistrationFailedError();
        } else {
            removeRegisteredChannel(channel);
            messageApi.unregisterForPushNotifications(channel, token).publish().connect();
        }
    }

    private void showUnregistrationFailedError() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new DisplayToast(this, getString(R.string.gcm_unregistration_failed)));
    }

    @ServiceAction
    void onTokenRefresh() {
        prefs.gcmRegistrationToken().remove();
        for (String channel : getRegisteredChannels()) {
            registerForPush(channel);
        }
    }

    private Set<String> getRegisteredChannels() {
        Set<String> registeredChannels = gson.fromJson(prefs.pushChannels().getOr(null),
                new TypeToken<Set<String>>() {
                }.getType());
        return registeredChannels == null ? new HashSet<>() : registeredChannels;
    }

    private void addRegisteredChannel(String channel) {
        Set<String> registeredChannels = getRegisteredChannels();
        registeredChannels.add(channel);
        saveRegisteredChannels(registeredChannels);
    }

    private void removeRegisteredChannel(String channel) {
        Set<String> registeredChannels = getRegisteredChannels();
        registeredChannels.remove(channel);
        saveRegisteredChannels(registeredChannels);
    }

    private void saveRegisteredChannels(Set<String> registeredChannels) {
        prefs.pushChannels().put(gson.toJson(registeredChannels));
    }

    /**
     * Retrieves this devices gcm registration token from the cache, fetches
     * a new one if it doesn't exist.
     */
    private String getToken() {
        String token = prefs.gcmRegistrationToken().get();
        if (token == null || token.isEmpty()) {
            token = fetchGcmRegistrationToken();
            prefs.gcmRegistrationToken().put(token);
        }
        Log.v(TAG, "Token: " + token);
        return token;
    }

    /**
     * Fetches a new token from the server.
     */
    private String fetchGcmRegistrationToken() {
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            return instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public class DisplayToast implements Runnable {
        private final Context context;
        String text;

        public DisplayToast(Context context, String text) {
            this.context = context;
            this.text = text;
        }

        public void run() {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }
}

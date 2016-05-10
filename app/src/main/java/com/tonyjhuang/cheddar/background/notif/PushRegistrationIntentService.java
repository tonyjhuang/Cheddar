package com.tonyjhuang.cheddar.background.notif;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.message.MessageApi;
import com.tonyjhuang.cheddar.api.simplepersist.GcmChannels;
import com.tonyjhuang.cheddar.api.simplepersist.SimplePersistApi;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.androidannotations.api.support.app.AbstractIntentService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by tonyjhuang on 3/1/16.
 */
@EIntentService
public class PushRegistrationIntentService extends AbstractIntentService {

    @Bean
    MessageApi messageApi;

    @Pref
    CheddarPrefs_ prefs;

    @Bean
    SimplePersistApi persistApi;

    public PushRegistrationIntentService() {
        super(PushRegistrationIntentService.class.getSimpleName());
    }

    @ServiceAction
    void unregisterAll() {
        for (String channel : persistApi.fetchGcmChannels().channels()) {
            unregisterForPush(channel);
        }
    }

    @ServiceAction
    void registerAll(List<String> channels) {
        Timber.d("registering for: " + channels);
        for (String channel : channels) {
            registerForPush(channel);
        }
    }

    @ServiceAction
    void registerForPush(String channel) {
        String token = getToken();
        if (token == null) {
            showRegistrationFailedError();
        } else {
            addRegisteredChannel(channel);
            messageApi.registerForPushNotifications(channel, token)
                    .doOnNext(result -> Timber.d("registered for push " + result))
                    .doOnError(e -> Timber.e(e, "failed to register for push"))
                    .publish().connect();
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
            Timber.e("COULDN'T RETRIEVE GCM TOKEN");
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
        GcmChannels channels = persistApi.fetchGcmChannels();
        return channels == null ? new HashSet<>() : channels.channels();
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
        persistApi.persist(GcmChannels.create(registeredChannels));
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
        Timber.v("Token: " + token);
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
            Timber.e(e.toString());
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

package com.tonyjhuang.cheddar.api;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.tonyjhuang.cheddar.BuildConfig;

import org.androidannotations.annotations.EBean;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;

@EBean(scope = EBean.Scope.Singleton)
public class MessageApi {

    public static final String PUBKEY = BuildConfig.PUBNUB_PUBKEY;
    public static final String SUBKEY = BuildConfig.PUBNUB_SUBKEY;
    private static final String TAG = MessageApi.class.getSimpleName();
    private Pubnub pubnub = new Pubnub(PUBKEY, SUBKEY);

    private Map<String, Observable<Object>> channelObservables = new HashMap<>();


    public Observable<Object> subscribe(String channel) {
        return Observable.defer(() -> {
            if (channelObservables.containsKey(channel)) {
                Log.d(TAG, "returning cached");
                return channelObservables.get(channel);
            }

            Log.d(TAG, "create new");
            PubnubObservableCallback callback = new PubnubObservableCallback() {
                @Override
                public void disconnectCallback(String channel, Object message) {
                    channelObservables.remove(channel);
                    super.disconnectCallback(channel, message);
                }
            };
            try {
                pubnub.subscribe(channel, callback);
            } catch (PubnubException e) {
                Log.e(TAG, "Error subscribing to channel: " + e.toString());
                callback.onError(e);
            }

            Observable<Object> channelObservable = callback.getObservable();
            channelObservables.put(channel, channelObservable);
            return channelObservable;
        });
    }

    public Observable<Void> unsubscribe(String channel) {
        return Observable.create(subscriber -> {
            Log.e(TAG, "unsubscribing from " + channel);
            pubnub.unsubscribe(channel, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    Log.e(TAG, "unsubscribed!");
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    subscriber.onError(new PubnubException(error));
                }

                @Override
                public void disconnectCallback(String channel, Object message) {
                    Log.e(TAG, "Disconnected..");
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
        });
    }

    public Observable<Object> registerForPushNotifications(String channel, String registrationToken) {
        return Observable.create(subscriber -> {
                    Log.d(TAG, "register..");
                    pubnub.enablePushNotificationsOnChannel(channel, registrationToken, new Callback() {
                        @Override
                        public void successCallback(String channel, Object message) {
                            Log.d(TAG, "registered for push: " + message);
                            subscriber.onNext(message);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            Log.d(TAG, "failed to register for push: " + error.toString());
                            subscriber.onError(new PubnubException(error));
                        }
                    });
                }
        );
    }

    public Observable<Object> unregisterForPushNotifications(String channel, String registrationToken) {
        return Observable.create(subscriber -> {
                    Log.d(TAG, "unregister..");
                    pubnub.disablePushNotificationsOnChannel(channel, registrationToken, new Callback() {
                        @Override
                        public void successCallback(String channel, Object message) {
                            Log.d(TAG, "unregistered for push: " + message);
                            subscriber.onNext(message);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            Log.d(TAG, "failed to unregister for push: " + error.toString());
                            subscriber.onError(new PubnubException(error));
                        }
                    });
                }
        );
    }

    /**
     * Propagates Pubnub events to a subscriber.
     */
    private static class PubnubObservableCallback extends Callback {

        private PubnubException error;
        private Subscriber<Object> subscriber;

        /**
         * Call if an PubnubException is thrown while subscribing/unsubscribing
         * to a channel.
         */
        public void onError(PubnubException error) {
            if (subscriber != null) {
                subscriber.onError(error);
            } else {
                this.error = error;
            }
        }

        @Override
        public void connectCallback(String channel, Object message) {
            Log.e(TAG, "Connected! " + message);
        }

        @Override
        public void successCallback(String channel, Object message) {
            Log.d(TAG, "got object: " + message.toString());
            if (subscriber != null) {
                subscriber.onNext(message);
            }
        }

        @Override
        public void reconnectCallback(String channel, Object message) {
            Log.d(TAG, "reconnected.. ");
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.e(TAG, "error connecting.. " + error.toString());
            if (subscriber != null) {
                subscriber.onError(new PubnubException(error));
                subscriber = null;
            }
        }

        @Override
        public void disconnectCallback(String channel, Object message) {
            Log.e(TAG, "Disconnected..");
            if (subscriber != null) {
                subscriber.onCompleted();
                subscriber = null;
            }
        }

        public Observable<Object> getObservable() {
            return Observable.defer(() ->
                    Observable.create(subscriber -> {
                        if (error != null) {
                            // Propagate cached error if there is one.
                            subscriber.onError(error);
                        } else {
                            this.subscriber = subscriber;
                        }
                    }));
        }
    }
}

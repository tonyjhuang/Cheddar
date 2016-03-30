package com.tonyjhuang.cheddar.api;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.tonyjhuang.cheddar.BuildConfig;

import org.androidannotations.annotations.EBean;

import rx.Observable;

@EBean(scope = EBean.Scope.Singleton)
public class MessageApi {

    public static final String PUBKEY = BuildConfig.PUBNUB_PUBKEY;
    public static final String SUBKEY = BuildConfig.PUBNUB_SUBKEY;
    private static final String TAG = MessageApi.class.getSimpleName();
    private Pubnub pubnub = new Pubnub(PUBKEY, SUBKEY);


    public Observable<Object> subscribe(String channel) {
        return Observable.create(subscriber -> {
            Log.e(TAG, "subscribing to " + channel);
            try {
                pubnub.subscribe(channel, new Callback() {
                    @Override
                    public void connectCallback(String channel, Object message) {
                        Log.e(TAG, "Connected! " + message);
                    }

                    @Override
                    public void successCallback(String channel, Object obj) {
                        Log.d(TAG, "got object: " + obj.toString());
                        subscriber.onNext(obj);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                        subscriber.onError(new Exception(error.toString()));
                    }

                    @Override
                    public void disconnectCallback(String channel, Object message) {
                        Log.e(TAG, "Disconnected..");
                        subscriber.onCompleted();
                    }
                });
            } catch (PubnubException e) {
                subscriber.onError(e);
            }
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
                    subscriber.onError(new Exception(error.toString()));
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
}

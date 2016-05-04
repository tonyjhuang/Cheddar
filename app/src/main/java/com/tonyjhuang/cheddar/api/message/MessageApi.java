package com.tonyjhuang.cheddar.api.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.api.models.value.ValueTypeAdapterFactory;
import com.tonyjhuang.cheddar.background.ConnectivityBroadcastReceiver;

import org.androidannotations.annotations.EBean;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

@EBean(scope = EBean.Scope.Singleton)
public class MessageApi {

    public static final String PUBKEY = BuildConfig.PUBNUB_PUBKEY;
    public static final String SUBKEY = BuildConfig.PUBNUB_SUBKEY;
    private Pubnub pubnub = new Pubnub(PUBKEY, SUBKEY);

    private Map<String, PubnubObservableCallback> channelObservables = new HashMap<>();

    public MessageApi() {
        super();
        ConnectivityBroadcastReceiver.connectionObservable
                .map(status -> status == ConnectivityBroadcastReceiver.Status.CONNECTED)
                .subscribe(isConnected -> pubnub.disconnectAndResubscribe());
    }

    public Observable<MessageApiObjectHolder> subscribe(String channel) {
        Timber.d("channel: " + channel);
        if (channelObservables.containsKey(channel)) {
            return channelObservables.get(channel).getObservable();
        } else {
            channelObservables.put(channel, new PubnubObservableCallback());
        }

        return Observable.defer(() -> {
            PubnubObservableCallback callback = channelObservables.get(channel);
            try {
                pubnub.subscribe(channel, callback);
                channelObservables.put(channel, callback);
            } catch (PubnubException e) {
                Timber.e("Error subscribing to channel: " + e.toString());
                callback.onError(e);
            }

            return callback.getObservable();
        });
    }

    public Observable<Void> unsubscribe(String channel) {
        return Observable.create(subscriber -> {
            channelObservables.remove(channel);
            pubnub.unsubscribe(channel, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    subscriber.onError(new PubnubException(error));
                }

                @Override
                public void disconnectCallback(String channel, Object message) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
        });
    }

    public Observable<Object> registerForPushNotifications(String channel, String registrationToken) {
        return Observable.create(subscriber -> {
                    pubnub.enablePushNotificationsOnChannel(channel, registrationToken, new Callback() {
                        @Override
                        public void successCallback(String channel, Object message) {
                            subscriber.onNext(message);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            subscriber.onError(new PubnubException(error));
                        }
                    });
                }
        );
    }

    public Observable<Object> unregisterForPushNotifications(String channel, String registrationToken) {
        return Observable.create(subscriber -> {
                    Timber.i("unregister..");
                    pubnub.disablePushNotificationsOnChannel(channel, registrationToken, new Callback() {
                        @Override
                        public void successCallback(String channel, Object message) {
                            subscriber.onNext(message);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            Timber.i("failed to unregister for push: " + error.toString());
                            subscriber.onError(new PubnubException(error));
                        }
                    });
                }
        );
    }

    /**
     * Propagates Pubnub events to a subscriber in the form of MessageApiObjectHolders.
     */
    private static class PubnubObservableCallback extends Callback {

        private Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new ValueTypeAdapterFactory())
                .registerTypeAdapter(MessageApiObjectHolder.class, new MessageApiDeserializer())
                .create();

        private PubnubException error;
        private Subscriber<? super MessageApiObjectHolder> subscriber;

        /**
         * Call if an PubnubException is thrown while subscribing/unsubscribing
         * to a channel.
         */
        public void onError(PubnubException error) {
            Timber.e(error, "onerror?");
            if (subscriber != null && !subscriber.isUnsubscribed()) {
                subscriber.onError(error);
            } else {
                this.error = error;
            }
        }

        @Override
        public void connectCallback(String channel, Object message) {
            Timber.i("Connected! " + message);
        }

        @Override
        public void successCallback(String channel, Object message) {
            Timber.v("got object: " + message.toString());
            if (subscriber != null && !subscriber.isUnsubscribed()) {
                subscriber.onNext(gson.fromJson(message.toString(), MessageApiObjectHolder.class));
            }

        }

        @Override
        public void reconnectCallback(String channel, Object message) {
            Timber.i("reconnected.. ");
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Timber.i("error.. " + error.toString());
        }

        @Override
        public void disconnectCallback(String channel, Object message) {
            Timber.i("Disconnected..");
        }

        public Observable<MessageApiObjectHolder> getObservable() {
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

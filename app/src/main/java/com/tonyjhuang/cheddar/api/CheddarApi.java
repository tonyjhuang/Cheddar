package com.tonyjhuang.cheddar.api;

import android.util.Log;

import com.parse.ParseUser;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.MessageEvent;

import org.androidannotations.annotations.EBean;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.parse.ParseObservable;


@EBean(scope = EBean.Scope.Singleton)
public class CheddarApi {

    private static final String TAG = CheddarApi.class.getSimpleName();
    private static final String PASSWORD = "password";

    private static final String PUBKEY = BuildConfig.PUBNUB_PUBKEY;
    private static final String SUBKEY = BuildConfig.PUBNUB_SUBKEY;

    private Pubnub pubnub = new Pubnub(PUBKEY, SUBKEY);

    public CheddarApi() {
    }

    public Observable<ParseUser> getCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "registering new user");
            return registerNewUser()
                    .flatMap((user) -> ParseObservable.logIn(user.getUsername(), PASSWORD));
        } else {
            Log.d(TAG, "returning user: " + currentUser.getObjectId());
            return Observable.just(currentUser);
        }
    }

    public Observable<Void> logout() {
        return ParseObservable.logOut();
    }

    public Observable<Alias> getAlias(String aliasId) {
        return ParseObservable.get(Alias.class, aliasId);
    }

    public Observable<ParseUser> registerNewUser() {
        return ParseObservable.callFunction("registerNewUser", new HashMap<>());
    }

    public Observable<Map<String, Object>> getDefaultParams(ParseUser user) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getObjectId());
        params.put("subkey", SUBKEY);
        params.put("pubkey", PUBKEY);
        return Observable.just(params);
    }

    public Observable<Alias> joinNextAvailableChatRoom(int maxOccupancy) {
        return getCurrentUser()
                .flatMap(this::getDefaultParams)
                .doOnNext((params) -> params.put("maxOccupancy", maxOccupancy))
                .flatMap(params -> ParseObservable.callFunction("joinNextAvailableChatRoom", params));
    }

    public Observable<Alias> leaveChatRoom(String aliasId) {
        Map<String, Object> params = new HashMap<>();
        params.put("aliasId", aliasId);
        return ParseObservable.callFunction("leaveChatRoom", params);
    }

    public Observable<Void> sendMessage(String aliasId, String body) {
        return getCurrentUser()
                .flatMap(this::getDefaultParams)
                .doOnNext((params) -> {
                    params.put("aliasId", aliasId);
                    params.put("body", body);
                    Log.e(TAG, "Calling with " + params);
                })
                .flatMap(params -> ParseObservable.callFunction("sendMessage", params))
                .map((object) -> null);
    }

    public Observable<Object> registerForPushNotifications(String aliasId, String registrationToken) {
        return getAlias(aliasId).flatMap((alias) ->
                        Observable.create(subscriber ->
                                        pubnub.enablePushNotificationsOnChannel(alias.getChatRoomId(), registrationToken, new Callback() {
                                            @Override
                                            public void successCallback(String channel, Object message) {
                                                Log.d(TAG, "registered for push: " + message);
                                                subscriber.onNext(message);
                                                subscriber.onCompleted();
                                            }

                                            @Override
                                            public void errorCallback(String channel, PubnubError error) {
                                                Log.d(TAG, "failed to register for push: " + error.toString());
                                                subscriber.onError(new Exception(error.toString()));
                                            }
                                        })
                        )
        );
    }

    public Observable<Object> unregisterForPushNotifications(String aliasId, String registrationToken) {
        return getAlias(aliasId).flatMap((alias) ->
                        Observable.create(subscriber ->
                                        pubnub.disablePushNotificationsOnChannel(alias.getChatRoomId(), registrationToken, new Callback() {
                                            @Override
                                            public void successCallback(String channel, Object message) {
                                                Log.d(TAG, "unregistered for push: " + message);
                                                subscriber.onNext(message);
                                                subscriber.onCompleted();
                                            }

                                            @Override
                                            public void errorCallback(String channel, PubnubError error) {
                                                Log.d(TAG, "failed to unregister for push: " + error.toString());
                                                subscriber.onError(new Exception(error.toString()));
                                            }
                                        })
                        )
        );
    }

    public Observable<MessageEvent> getMessageStream(String aliasId) {
        return ParseObservable.get(Alias.class, aliasId).flatMap((alias ->
                Observable.create(subscriber -> {
                    try {
                        pubnub.subscribe(alias.getChatRoomId(), new Callback() {
                            @Override
                            public void successCallback(String channel, Object obj) {
                                Log.d(TAG, "WHAT IS THIS: " + obj);
                                subscriber.onNext(obj);
                            }

                            @Override
                            public void errorCallback(String channel, PubnubError error) {
                                subscriber.onError(new Exception(error.toString()));
                            }

                            @Override
                            public void disconnectCallback(String channel, Object message) {
                                subscriber.onCompleted();
                            }
                        });
                    } catch (PubnubException e) {
                        subscriber.onError(e);
                    }
                })
        )).cast(JSONObject.class).flatMap(MessageEventParser::parse);
    }
}

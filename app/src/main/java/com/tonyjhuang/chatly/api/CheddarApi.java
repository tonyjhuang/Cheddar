package com.tonyjhuang.chatly.api;

import android.util.Log;

import com.parse.ParseUser;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.tonyjhuang.chatly.BuildConfig;
import com.tonyjhuang.chatly.api.models.Alias;
import com.tonyjhuang.chatly.api.models.Message;

import org.androidannotations.annotations.EBean;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.parse.ParseObservable;


@EBean(scope = EBean.Scope.Singleton)
public class CheddarApi {
    private static final String PASSWORD = "password";

    private static final String PUBKEY = BuildConfig.PUBNUB_PUBKEY;
    private static final String SUBKEY = BuildConfig.PUBNUB_SUBKEY;

    private Pubnub pubnub = new Pubnub(PUBKEY, SUBKEY);

    public CheddarApi () {}

    public Observable<ParseUser> getCurrentUser() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            Log.d("Cheddar", "registering new user");
            return registerNewUser()
                    .flatMap((user) -> ParseObservable.logIn(user.getUsername(), PASSWORD));
        } else {
            Log.d("Cheddar", "returning user: " + currentUser.getObjectId());
            return Observable.just(currentUser);
        }
    }

    public Observable<Alias> getAlias(String aliasId) {
        return ParseObservable.get(Alias.class, aliasId);
    }

    public Observable<ParseUser> registerNewUser() {
        return ParseObservable.callFunction("registerNewUser", new HashMap<>());
    }

    public Observable<Map<String, Object>> getUserIdParams(ParseUser user) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.getObjectId());
        return Observable.just(params);
    }

    public Observable<Alias> joinNextAvailableChatRoom(int maxOccupancy) {
        return joinNextAvailableChatRoomCast(maxOccupancy);
    }

    private Observable<Alias> joinNextAvailableChatRoomCast(int maxOccupancy) {
        return getCurrentUser()
                .flatMap(this::getUserIdParams)
                .doOnNext((params) -> params.put("maxOccupancy", maxOccupancy))
                .flatMap(params -> ParseObservable.callFunction("joinNextAvailableChatRoom", params));
    }

    public Observable<Alias> leaveChatRoom(String aliasId) {
        Map<String, Object> params = new HashMap<>();
        params.put("aliasId", aliasId);
        return ParseObservable.callFunction("leaveChatRoom", params);
    }

    public Observable<Message> sendMessage(String aliasId, String body) {
        return getCurrentUser()
                .flatMap(this::getUserIdParams)
                .doOnNext((params) -> {
                    params.put("aliasId", aliasId);
                    params.put("body", body);
                    params.put("pubkey", PUBKEY);
                    params.put("subkey", SUBKEY);
                    Log.e("API", "Calling with " + params);
                })
                .flatMap(params -> ParseObservable.callFunction("sendMessage", params));
    }

    public Observable<Message> getMessageStream(String aliasId) {
        return ParseObservable.get(Alias.class, aliasId).flatMap((alias ->
            Observable.create(subscriber -> {
                try {
                    pubnub.subscribe(alias.getChatRoomId(), new Callback() {
                        @Override
                        public void successCallback(String channel, Object obj) {
                            Message message = Message.fromJson((JSONObject) obj);
                            if(message != null) {
                                Log.e("API", message.toString());
                            }
                            subscriber.onNext(message);
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
        ));
    }
}

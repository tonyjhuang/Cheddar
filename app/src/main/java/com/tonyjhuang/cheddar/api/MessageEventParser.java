package com.tonyjhuang.cheddar.api;

import android.util.Log;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.MessageEvent;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;

/**
 * Created by tonyjhuang on 3/2/16.
 */
public class MessageEventParser {
    private static final String TAG = MessageEventParser.class.getSimpleName();

    private static final String MESSAGE_EVENT = "messageEvent";
    private static final String DATA_OBJECT = "object";
    private static final String DATA_OBJECT_TYPE = "objectType";

    public static Observable<MessageEvent> parse(JSONObject object) {
        return Observable.create(subscriber -> {
            Log.d(TAG, "object: " + object.toString());
            try {
                Log.d(TAG, "objectobject: " + object.getJSONObject(DATA_OBJECT));
                switch (object.getString(DATA_OBJECT_TYPE)) {
                    case MESSAGE_EVENT:
                        subscriber.onNext(Message.fromJson(object.getJSONObject(DATA_OBJECT)));
                        break;
                    default:
                        Log.e(TAG, "unrecognized object.");
                        subscriber.onError(new UnrecognizedParseException());
                        return;
                }

                subscriber.onCompleted();
            } catch (JSONException | ClassCastException e) {
                subscriber.onError(e);
            }
        });
    }

    public static class UnrecognizedParseException extends Exception {
    }
}

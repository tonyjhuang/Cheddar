package com.tonyjhuang.cheddar.api;

import android.util.Log;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.MessageEvent;
import com.tonyjhuang.cheddar.api.models.Presence;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;

/**
 * Created by tonyjhuang on 3/2/16.
 */
public class MessageEventParser {
    private static final String TAG = MessageEventParser.class.getSimpleName();

    private static final String DATA_OBJECT = "object";
    private static final String DATA_OBJECT_TYPE = "objectType";

    public static Observable<MessageEvent> parse(JSONObject object) {
        return Observable.create(subscriber -> {
            try {
                JSONObject data = object.getJSONObject(DATA_OBJECT);
                switch (ObjectType.valueOf(object.getString(DATA_OBJECT_TYPE))) {
                    case messageEvent:
                        subscriber.onNext(Message.fromJson(data));
                        break;
                    case presenceEvent:
                        subscriber.onNext(Presence.fromJson(data));
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

    private enum ObjectType {
        messageEvent, presenceEvent
    }

    public static class UnrecognizedParseException extends Exception {
    }
}

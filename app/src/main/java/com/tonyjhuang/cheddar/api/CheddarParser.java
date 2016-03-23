package com.tonyjhuang.cheddar.api;

import com.tonyjhuang.cheddar.api.models.Message;
import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.api.models.Presence;

import org.json.JSONException;
import org.json.JSONObject;

import rx.Observable;

/**
 * Created by tonyjhuang on 3/14/16.
 */
public class CheddarParser {

    private static final String TAG = CheddarParser.class.getSimpleName();

    private static final String DATA_OBJECT = "object";
    private static final String DATA_OBJECT_TYPE = "objectType";

    public static ChatEvent parseMessageEvent(JSONObject object) throws UnrecognizedParseException {
        try {
            JSONObject data = object.getJSONObject(DATA_OBJECT);
            switch (MessageEventObjectType.valueOf(object.getString(DATA_OBJECT_TYPE))) {
                case messageEvent:
                    return Message.fromJson(data);
                case presenceEvent:
                    return Presence.fromJson(data);
                default:
                    throw new UnrecognizedParseException("unrecognized objectType");
            }
        } catch (JSONException e) {
            throw new UnrecognizedParseException("failed to parse json");
        }
    }

    public static Observable<ChatEvent> parseMessageEventRx(JSONObject object) {
        return Observable.create((subscriber -> {
            try {
                subscriber.onNext(parseMessageEvent(object));
                subscriber.onCompleted();
            } catch (UnrecognizedParseException e) {
                subscriber.onError(e);
            }
        }));
    }

    public enum MessageEventObjectType {
        messageEvent, presenceEvent
    }

    public static class UnrecognizedParseException extends Exception {
        public String reason;
        public UnrecognizedParseException(String reason) {
            this.reason = reason;
        }
    }
}

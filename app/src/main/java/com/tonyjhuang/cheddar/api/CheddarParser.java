package com.tonyjhuang.cheddar.api;

import android.util.Log;

import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.api.models.ChatRoomInfo;

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

    public static ChatEvent parseChatEvent(JSONObject object) throws UnparseableException {
        try {
            JSONObject data = object.getJSONObject(DATA_OBJECT);
            try {
                Type type = Type.valueOf(object.getString(DATA_OBJECT_TYPE));
                switch (type) {
                    case ChatEvent:
                        return ChatEvent.fromJson(data);
                    default:
                        Log.e(TAG, "Unhandled Type " + type);
                        throw new UnparseableException("unrecognized objectType");
                }
            } catch (IllegalArgumentException e) {
                String dataType = object.getString(DATA_OBJECT_TYPE);
                Log.e(TAG, "Not a valid Data Object type: " + dataType);
                throw new UnparseableException("unrecognized data object type: " + dataType);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse json: " + e.toString());
            throw new UnparseableException("failed to parse json");
        }
    }

    public static Observable<ChatEvent> parseChatEventRx(JSONObject object) {
        return Observable.create((subscriber -> {
            try {
                subscriber.onNext(parseChatEvent(object));
                subscriber.onCompleted();
            } catch (UnparseableException e) {
                subscriber.onError(e);
            }
        }));
    }

    public static Observable<ChatEvent> parseChatEventRxSkippable(JSONObject object) {
        return parseChatEventRx(object).onExceptionResumeNext(Observable.empty());
    }

    public static Observable<ChatRoomInfo> parseChatRoomInfoRx(JSONObject object) {
        return Observable.create((subscriber -> {
            try {
                subscriber.onNext(ChatRoomInfo.fromJson(object));
                subscriber.onCompleted();
            } catch (JSONException e) {
                subscriber.onError(e);
            }
        }));
    }

    public enum Type {
        ChatEvent
    }

    public static class UnparseableException extends Exception {
        public String reason;

        public UnparseableException(String reason) {
            this.reason = reason;
        }
    }
}

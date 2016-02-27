package com.tonyjhuang.chatly.api.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.tonyjhuang.chatly.api.Time;

import org.json.JSONException;
import org.json.JSONObject;

@ParseClassName("Message")
public class Message extends ParseObject {

    public static Message createPlaceholderMessage(Alias alias, String body) {
        Message message = new Message();
        message.put("body", body);
        message.put("alias", alias);
        return message;
    }

    public static Message fromJson(JSONObject object) {
        try {
            Message message = ParseObject.createWithoutData(Message.class, object.getString("objectId"));
            message.put("alias", Alias.fromJson(object.getJSONObject("alias")));
            message.put("body", object.getString("body"));
            message.put("createdAt", Time.getDate(object.getString("createdAt")));
            message.put("updatedAt", Time.getDate(object.getString("updatedAt")));
            return message;
        } catch (JSONException e) {
            Log.e("Message", e.toString());
            return null;
        }
    }

    public Alias getAlias() {
        return (Alias) getParseObject("alias");
    }

    public String getBody() {
        return getString("body");
    }

    @Override
    public String toString() {
        return "{" +
                getObjectId() +
                "|=> alias: " + getAlias() +
                ", body: " + getBody() +
                "}";

    }
}

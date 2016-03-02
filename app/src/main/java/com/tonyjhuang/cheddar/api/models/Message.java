package com.tonyjhuang.cheddar.api.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.tonyjhuang.cheddar.api.Time;

import org.json.JSONException;
import org.json.JSONObject;

@ParseClassName("Message")
public class Message extends MessageEvent {

    public static Message createPlaceholderMessage(Alias alias, String body) {
        Message message = new Message();
        message.put("body", body);
        message.put("alias", alias);
        message.setType(Type.MESSAGE);
        return message;
    }

    public static Message fromJson(JSONObject object) throws JSONException {
        Message message = ParseObject.createWithoutData(Message.class, object.getString("objectId"));
        message.put("alias", Alias.fromJson(object.getJSONObject("alias")));
        message.put("body", object.getString("body"));
        message.put("createdAt", Time.getDate(object.getString("createdAt")));
        message.put("updatedAt", Time.getDate(object.getString("updatedAt")));
        message.setType(Type.MESSAGE);
        return message;
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

package com.tonyjhuang.cheddar.api.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.tonyjhuang.cheddar.api.Time;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

@ParseClassName("Message")
public class Message extends ParseObject implements ChatEvent {

    private Type type;

    public static Message createPlaceholderMessage(Alias alias, String body) {
        // TODO: HORRIBLE HORRIBLE HACK, ALL TIMES PASSED FROM SERVER ARE IN GMT
        // TODO: BUT WE
        Date now = new Date(System.currentTimeMillis());
        Log.d("MESSAGE", "CREATED NEW DATE AT " + now.toString());
        Message message = new Message();
        message.put("body", body);
        message.put("alias", alias);
        message.put("createdAt", now);
        message.put("updatedAt", now);
        message.setType(Type.MESSAGE);
        return message;
    }

    public static Message fromJson(JSONObject object) throws JSONException {
        Message message = ParseObject.createWithoutData(Message.class, object.getString("objectId"));
        message.put("alias", Alias.fromJson(object.getJSONObject("alias")));
        message.put("body", object.getString("body"));
        message.put("createdAt", Time.getDateAsUTC(object.getString("createdAt")));
        message.put("updatedAt", Time.getDateAsUTC(object.getString("updatedAt")));
        message.setType(Type.MESSAGE);
        return message;
    }

    /**
     * We need to provide this override since we're creating Alias objects from
     * JSONObjects using createWithoutData() and put("createdAt") does not
     * update the ParseObject's internal createdAt Date.
     */
    @Override
    public Date getCreatedAt() {
        return super.getCreatedAt() == null ? (Date) get("createdAt") : super.getCreatedAt();
    }

    /**
     * See getCreatedAt
     */
    @Override
    public Date getUpdatedAt() {
        return super.getUpdatedAt() == null ? (Date) get("updatedAt") : super.getUpdatedAt();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
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

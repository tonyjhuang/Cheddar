package com.tonyjhuang.chatly.api.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.tonyjhuang.chatly.api.Time;

import org.json.JSONException;
import org.json.JSONObject;

@ParseClassName("Alias")
public class Alias extends ParseObject {

    public static Alias fromJson(JSONObject object) {
        try {
            Alias alias = ParseObject.createWithoutData(Alias.class, object.getString("objectId"));
            alias.put("name", object.getString("name"));
            alias.put("active", object.getBoolean("active"));
            alias.put("chatRoomId", object.getString("chatRoomId"));
            alias.put("userId", object.getString("userId"));
            alias.put("createdAt", Time.getDate(object.getString("createdAt")));
            alias.put("updatedAt", Time.getDate(object.getString("updatedAt")));
            return alias;
        } catch (JSONException e) {
            Log.e("Alias", e.toString());
            return null;
        }
    }

    public String getName() {
        return getString("name");
    }

    public boolean isActive() {
        return getBoolean("active");
    }

    public String getChatRoomId() {
        return getString("chatRoomId");
    }

    public String getUserId() {
        return getString("userId");
    }

    @Override
    public String toString() {
        return "{" +
                getObjectId() +
                "|=> name: " + getName() +
                ", active: " + isActive() +
                ", chatRoomId: " + getChatRoomId() +
                ", userId: " + getUserId() +
                "}";
    }
}

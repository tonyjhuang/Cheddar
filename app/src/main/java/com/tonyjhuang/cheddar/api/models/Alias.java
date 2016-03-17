package com.tonyjhuang.cheddar.api.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.tonyjhuang.cheddar.api.Time;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

@ParseClassName("Alias")
public class Alias extends JSONParseObject{

    public static Alias fromJson(JSONObject object) throws JSONException {
        Alias alias = ParseObject.createWithoutData(Alias.class, object.getString("objectId"));
        alias.put("name", object.getString("name"));
        alias.put("active", object.getBoolean("active"));
        alias.put("chatRoomId", object.getString("chatRoomId"));
        alias.put("userId", object.getString("userId"));
        alias.put("createdAt", Time.getDateAsUTC(object.getString("createdAt")));
        alias.put("updatedAt", Time.getDateAsUTC(object.getString("updatedAt")));
        return alias;
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

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
}

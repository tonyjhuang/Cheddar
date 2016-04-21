package com.tonyjhuang.cheddar.api.models.parse;

import com.parse.ParseClassName;
import com.tonyjhuang.cheddar.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

@ParseClassName("ParseAlias")
public class ParseAlias extends JSONParseObject {

    public static ParseAlias fromJson(JSONObject object) throws JSONException {
        ParseAlias parseAlias = createWithoutData(ParseAlias.class, object.getString("objectId"));
        parseAlias.put("name", object.getString("name"));
        parseAlias.put("active", object.getBoolean("active"));
        parseAlias.put("chatRoomId", object.getString("chatRoomId"));
        parseAlias.put("userId", object.getString("userId"));
        parseAlias.put("createdAt", TimeUtils.getDateAsUTC(object.getString("createdAt")));
        parseAlias.put("updatedAt", TimeUtils.getDateAsUTC(object.getString("updatedAt")));
        return parseAlias;
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

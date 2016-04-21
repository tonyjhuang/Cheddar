package com.tonyjhuang.cheddar.api.models.parse;

import com.parse.ParseClassName;
import com.tonyjhuang.cheddar.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

@ParseClassName("ParseChatRoom")
public class ParseChatRoom extends JSONParseObject {

    public static ParseChatRoom fromJson(JSONObject object) throws JSONException {
        ParseChatRoom parseChatRoom = createWithoutData(ParseChatRoom.class, object.getString("objectId"));
        parseChatRoom.put("maxOccupancy", object.getInt("maxOccupancy"));
        parseChatRoom.put("numOccupants", object.getInt("numOccupants"));
        parseChatRoom.put("createdAt", TimeUtils.getDateAsUTC(object.getString("createdAt")));
        parseChatRoom.put("updatedAt", TimeUtils.getDateAsUTC(object.getString("updatedAt")));
        return parseChatRoom;
    }

    public int getMaxOccupancy() {
        return getInt("maxOccupancy");
    }

    public int getNumOccupants() {
        return getInt("numOccupants");
    }

    public String toString() {
        return "{" +
                getObjectId() +
                "|=> maxOccupancy: " + getMaxOccupancy() +
                ", numOccupants: " + getNumOccupants() +
                "}";
    }
}

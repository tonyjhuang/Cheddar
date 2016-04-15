package com.tonyjhuang.cheddar.api.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.tonyjhuang.cheddar.api.Time;

import org.json.JSONException;
import org.json.JSONObject;

@ParseClassName("ChatRoom")
public class ChatRoom extends JSONParseObject {

    public static ChatRoom fromJson(JSONObject object) throws JSONException {
        ChatRoom chatRoom = ParseObject.createWithoutData(ChatRoom.class, object.getString("objectId"));
        chatRoom.put("maxOccupancy", object.getInt("maxOccupancy"));
        chatRoom.put("numOccupants", object.getInt("numOccupants"));
        chatRoom.put("createdAt", Time.getDateAsUTC(object.getString("createdAt")));
        chatRoom.put("updatedAt", Time.getDateAsUTC(object.getString("updatedAt")));
        return chatRoom;
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

package com.tonyjhuang.cheddar.api.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.tonyjhuang.cheddar.api.Time;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

@ParseClassName("ChatEvent")
public class ChatEvent extends JSONParseObject {

    public static ChatEvent createPlaceholderMessage(Alias alias, String body) {
        Date now = new Date();
        ChatEvent chatEvent = new ChatEvent();
        chatEvent.put("createdAt", now);
        chatEvent.put("updatedAt", now);
        chatEvent.put("alias", alias);
        chatEvent.put("body", body);
        chatEvent.setType(Type.MESSAGE);
        return chatEvent;
    }

    public static ChatEvent fromJson(JSONObject object) throws JSONException {
        ChatEvent chatEvent = ParseObject.createWithoutData(ChatEvent.class, object.getString("objectId"));
        chatEvent.put("createdAt", Time.getDateAsUTC(object.getString("createdAt")));
        chatEvent.put("updatedAt", Time.getDateAsUTC(object.getString("updatedAt")));
        chatEvent.put("alias", Alias.fromJson(object.getJSONObject("alias")));
        chatEvent.put("body", object.getString("body"));
        chatEvent.setType(Type.fromString(object.getString("type")));
        return chatEvent;
    }

    public Type getType() {
        return Type.fromString(getString("type"));
    }

    private void setType(Type type) {
        put("type", type.toString());
    }

    public Alias getAlias() {
        return (Alias) getParseObject("alias");
    }

    public String getBody() {
        return getString("body");
    }

    // Convenience method.
    public String getChatRoomId() {
        return getAlias().getChatRoomId();
    }

    public enum Type {
        MESSAGE, PRESENCE;

        public static Type fromString(String string) {
            return Type.valueOf(string.toUpperCase(Locale.US));
        }
    }
}

package com.tonyjhuang.cheddar.api.models.parse;

import com.parse.ParseClassName;
import com.tonyjhuang.cheddar.utils.TimeUtils;

import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

@ParseClassName("ParseChatEvent")
public class ParseChatEvent extends JSONParseObject {

    public static ParseChatEvent createPlaceholderMessage(ParseAlias parseAlias, String body) {
        Date now = new Date();
        ParseChatEvent parseChatEvent = new ParseChatEvent();
        parseChatEvent.put("createdAt", now);
        parseChatEvent.put("updatedAt", now);
        parseChatEvent.put("alias", parseAlias);
        parseChatEvent.put("body", body);
        parseChatEvent.setType(Type.MESSAGE);
        return parseChatEvent;
    }

    public static ParseChatEvent fromJson(JSONObject object) throws JSONException {
        ParseChatEvent parseChatEvent = createWithoutData(ParseChatEvent.class, object.getString("objectId"));
        parseChatEvent.put("createdAt", TimeUtils.getDateAsUTC(object.getString("createdAt")));
        parseChatEvent.put("updatedAt", TimeUtils.getDateAsUTC(object.getString("updatedAt")));
        parseChatEvent.put("alias", ParseAlias.fromJson(object.getJSONObject("alias")));
        parseChatEvent.put("body", object.getString("body"));
        parseChatEvent.setType(Type.fromString(object.getString("type")));
        return parseChatEvent;
    }

    public Type getType() {
        return Type.fromString(getString("type"));
    }

    private void setType(Type type) {
        put("type", type.toString());
    }

    public ParseAlias getAlias() {
        return (ParseAlias) getParseObject("alias");
    }

    public String getBody() {
        return getString("body");
    }

    /**
     * How this ParseChatEvent should be displayed in notification or non-chat contexts.
     */
    public String getDisplayBody() {
        switch (getType()) {
            case MESSAGE:
                return WordUtils.capitalizeFully(getAlias().getName() + ": " + getBody());
            case PRESENCE:
                return getBody();
            default:
                return "";
        }
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

package com.tonyjhuang.cheddar.api.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tonyjhuang on 3/3/16.
 */
public class Presence implements ChatEvent {

    private Type type;

    private Action action;
    private Alias alias;

    public static Presence fromJson(JSONObject object) throws JSONException {
        Presence presence = new Presence();
        presence.alias = Alias.fromJson(object.getJSONObject("alias"));
        presence.action = Action.valueOf(object.getString("action").toUpperCase());
        presence.setType(ChatEvent.Type.PRESENCE);
        return presence;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    public Action getAction() {
        return action;
    }

    public Alias getAlias() {
        return alias;
    }

    public enum Action {
        JOIN, LEAVE
    }

    @Override
    public String toString() {
        return "{" +
                "|=> alias: " + alias +
                ", action: " + action +
                "}";

    }
}

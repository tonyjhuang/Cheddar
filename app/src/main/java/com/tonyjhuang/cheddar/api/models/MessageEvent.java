package com.tonyjhuang.cheddar.api.models;

import com.parse.ParseObject;

/**
 * Created by tonyjhuang on 3/2/16.
 */
public abstract class MessageEvent extends ParseObject {
    private Type type = Type.UNKNOWN;

    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        MESSAGE, PRESENCE, UNKNOWN
    }
}

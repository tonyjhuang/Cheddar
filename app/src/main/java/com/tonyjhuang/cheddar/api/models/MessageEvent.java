package com.tonyjhuang.cheddar.api.models;

/**
 * Created by tonyjhuang on 3/2/16.
 */
public interface MessageEvent {

    Type getType();

    void setType(Type type);

    enum Type {
        MESSAGE, PRESENCE, UNKNOWN
    }
}

package com.tonyjhuang.cheddar.api.models;

/**
 * Created by tonyjhuang on 3/2/16.
 */
public interface ChatEvent {

    Type getType();

    void setType(Type type);

    Alias getAlias();

    enum Type {
        MESSAGE, PRESENCE, UNKNOWN
    }
}

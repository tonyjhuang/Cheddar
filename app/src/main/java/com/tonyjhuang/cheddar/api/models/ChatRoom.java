package com.tonyjhuang.cheddar.api.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("ChatRoom")
public class ChatRoom extends ParseObject {

    public ChatRoom() {
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

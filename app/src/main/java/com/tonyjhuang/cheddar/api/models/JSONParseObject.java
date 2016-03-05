package com.tonyjhuang.cheddar.api.models;

import com.parse.ParseObject;
import com.tonyjhuang.cheddar.api.Time;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by tonyjhuang on 3/4/16.
 */
public abstract class JSONParseObject extends ParseObject {

    public JSONObject toJson() {
        HashMap<String, Object> map = new HashMap<>();
        for (String key : keySet()) {
            Object value = get(key);
            if (value instanceof JSONParseObject) {
                map.put(key, ((JSONParseObject) value).toJson());
            } else {
                map.put(key, value);
            }
        }
        map.put("createdAt", Time.toString(getCreatedAt()));
        map.put("updatedAt", Time.toString(getUpdatedAt()));
        map.put("objectId", getObjectId());
        return new JSONObject(map);
    }
}

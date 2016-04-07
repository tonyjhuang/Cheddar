package com.tonyjhuang.cheddar.api.models;

import com.parse.ParseObject;
import com.tonyjhuang.cheddar.api.Time;

import org.json.JSONObject;

import java.util.Date;
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

    /**
     * We need to provide this override since we're creating Alias objects from
     * JSONObjects using createWithoutData() and put("createdAt") does not
     * update the ParseObject's internal createdAt Date.
     */
    @Override
    public Date getCreatedAt() {
        return super.getCreatedAt() == null ? (Date) get("createdAt") : super.getCreatedAt();
    }

    /**
     * See getCreatedAt
     */
    @Override
    public Date getUpdatedAt() {
        return super.getUpdatedAt() == null ? (Date) get("updatedAt") : super.getUpdatedAt();
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}

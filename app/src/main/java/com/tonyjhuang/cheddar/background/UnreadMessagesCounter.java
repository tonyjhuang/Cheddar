package com.tonyjhuang.cheddar.background;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.tonyjhuang.cheddar.CheddarPrefs_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import rx.Observable;

/**
 * Keeps track of the number of unread notifications for each chat room.
 * Persists across app closes.
 */
@EBean
public class UnreadMessagesCounter {

    private static final String TAG = UnreadMessagesCounter.class.getSimpleName();

    @Pref
    CheddarPrefs_ prefs;

    public void increment(String chatRoomId) {
        save(chatRoomId, get(chatRoomId) + 1);
    }

    public void clear(String chatRoomId) {
        save(chatRoomId, 0);
    }

    public int get(String chatRoomId) {
        return loadMap().get(chatRoomId);
    }

    private void save(String chatRoomId, int value) {
        Map<String, Integer> map = loadMap();
        map.put(chatRoomId, value);
        saveMap(map);
    }

    private void saveMap(Map<String, Integer> inputMap) {
        JSONObject jsonObject = new JSONObject(inputMap);
        String jsonString = jsonObject.toString();
        prefs.unreadMessages().put(jsonString);
    }

    private Map<String, Integer> loadMap() {
        Map<String, Integer> outputMap = new HashMap<>();
        try {
                String jsonString = prefs.unreadMessages().getOr("");
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    int value = (Integer) jsonObject.get(key);
                    outputMap.put(key, value);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return outputMap;
    }
}

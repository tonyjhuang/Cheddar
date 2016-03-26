package com.tonyjhuang.cheddar.background;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tonyjhuang.cheddar.CheddarPrefs_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of the number of unread notifications for each chat room.
 * Persists across app closes.
 */
@EBean
public class UnreadMessagesCounter {

    private static final String TAG = UnreadMessagesCounter.class.getSimpleName();
    private static final Gson gson = new Gson();

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
        prefs.unreadMessages().put(gson.toJson(inputMap));
    }

    private Map<String, Integer> loadMap() {
        Map<String, Integer> map = gson.fromJson(prefs.unreadMessages().get(),
                new TypeToken<Map<String, Integer>>() {}.getType());

        if (map == null) {
            map = new HashMap<>();
        }

        return map;
    }
}

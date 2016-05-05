package com.tonyjhuang.cheddar.background;

import com.tonyjhuang.cheddar.api.simplepersist.SimplePersistApi;
import com.tonyjhuang.cheddar.api.simplepersist.UnreadMessages;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.Map;

/**
 * Keeps track of the number of unread notifications for each chat room.
 * Persists across app closes.
 */
@EBean(scope = EBean.Scope.Singleton)
public class UnreadMessagesCounter {

    @Bean
    SimplePersistApi persistApi;

    public void increment(String chatRoomId) {
        save(chatRoomId, get(chatRoomId) + 1);
    }

    public void clear(String chatRoomId) {
        save(chatRoomId, 0);
    }

    public int get(String chatRoomId) {
        Map<String, Integer> map = persistApi.fetchUnreadMessages().messages();
        return map.containsKey(chatRoomId) ? map.get(chatRoomId) : 0;
    }

    private void save(String chatRoomId, int value) {
        UnreadMessages messages = persistApi.fetchUnreadMessages();
        messages.messages().put(chatRoomId, value);
        persistApi.persist(messages);
    }
}

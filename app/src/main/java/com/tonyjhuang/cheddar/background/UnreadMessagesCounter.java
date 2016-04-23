package com.tonyjhuang.cheddar.background;

import com.tonyjhuang.cheddar.api.simplepersist.SimplePersistApi;
import com.tonyjhuang.cheddar.api.simplepersist.UnreadMessages;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import timber.log.Timber;

/**
 * Keeps track of the number of unread notifications for each chat room.
 * Persists across app closes.
 */
@EBean
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
        return persistApi.fetchUnreadMessages().messages().get(chatRoomId);
    }

    private void save(String chatRoomId, int value) {
        UnreadMessages messages = persistApi.fetchUnreadMessages();
        Timber.d(messages.toString());
        messages.messages().put(chatRoomId, value);
        persistApi.persist(messages);
    }
}

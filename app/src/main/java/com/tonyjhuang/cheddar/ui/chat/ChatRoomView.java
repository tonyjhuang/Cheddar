package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.ChatEvent;
import com.tonyjhuang.cheddar.api.models.Message;

import java.util.List;

/**
 * Created by tonyjhuang on 3/17/16.
 */
public interface ChatRoomView {

    /**
     * Incoming messages are new.
     */
    void displayNewChatEvents(String currentUserId, List<ChatEvent> messages);

    /**
     * Incoming messages are from history.
     */
    void displayOldChatEvents(String currentUserId, List<ChatEvent> messages);

    void displayLoadHistoryChatEventsError();

    void displayPlaceholderMessage(Message message);

    void notifyPlaceholderMessageFailed(Message message);

    void navigateToListView();
}
package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.Alias;
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

    void displayActiveAliases(List<Alias> aliases, String currentUserId);

    void displayLoadHistoryChatEventsError();

    void displayPlaceholderMessage(Message message);

    void displayNetworkConnectionError();

    void hideNetworkConnectionError();

    void notifyPlaceholderMessageFailed(Message message);

    void navigateToListView();
}

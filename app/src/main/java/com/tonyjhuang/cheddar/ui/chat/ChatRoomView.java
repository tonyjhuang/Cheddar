package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;

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

    void displayPlaceholderMessage(ChatEvent message);

    void displayNetworkConnectionError();

    void hideNetworkConnectionError();

    void notifyPlaceholderMessageFailed(ChatEvent message);

    /**
     * Tells this View that there are no more ChatEvents for this ChatRoom.
     */
    void notifyEndOfChatEvents();

    void navigateToListView();
}

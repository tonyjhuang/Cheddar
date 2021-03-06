package com.tonyjhuang.cheddar.ui.chat;

import com.tonyjhuang.cheddar.ui.presenter.Presenter;

/**
 * Created by tonyjhuang on 3/17/16.
 */
public interface ChatRoomPresenter extends Presenter<ChatRoomView> {

    void setAliasId(String aliasId);

    /**
     * Notifies the presenter that the View is ready to start receiving items (again).
     */
    void onResume();

    /**
     * Notifies the presenter that any new items for the View should be cached
     * until onResume is called or thrown away in onDestroy.
     */
    void onPause();

    void leaveChatRoom();

    void sendMessage(String message);

    void loadMoreMessages();

    void onDestroy();

    void sendFeedback(String feedback);

    void updateChatRoomName(String name);
}

package com.tonyjhuang.cheddar.ui.list;

import com.tonyjhuang.cheddar.ui.presenter.Presenter;

/**
 * Supplies data to and handles events from
 * RoomListViews.
 */
public interface ChatRoomListPresenter extends Presenter<ChatRoomListView> {

    void onJoinChatRoomClicked();

    void logout();

    void debugReset();

    void onResume();

    void onPause();

    void onDestroy();
}

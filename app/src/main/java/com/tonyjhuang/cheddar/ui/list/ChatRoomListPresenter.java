package com.tonyjhuang.cheddar.ui.list;

import com.tonyjhuang.cheddar.presenter.Presenter;

/**
 * Supplies data to and handles events from
 * RoomListViews.
 */
public interface ChatRoomListPresenter extends Presenter<ChatRoomListView> {

    void onJoinChatRoomClicked();

    void onResume();

    void onPause();

    void onDestroy();
}

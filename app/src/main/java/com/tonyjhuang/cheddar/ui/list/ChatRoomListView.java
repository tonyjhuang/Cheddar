package com.tonyjhuang.cheddar.ui.list;

import com.tonyjhuang.cheddar.api.models.ChatRoomInfo;

import java.util.List;

/**
 * Created by tonyjhuang on 4/14/16.
 */
public interface ChatRoomListView {

    void displayList(List<ChatRoomInfo> infoList);

    void navigateToChatView(String aliasId);

    void showJoinChatError();
}

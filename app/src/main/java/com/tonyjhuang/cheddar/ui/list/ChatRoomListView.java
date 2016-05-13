package com.tonyjhuang.cheddar.ui.list;

import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;

import java.util.List;

/**
 * View for the list of the current User's ChatRooms.
 */
public interface ChatRoomListView {

    void displayList(List<ChatRoomInfo> infoList, String currentUserId);

    void displayUserEmail(String email);

    void navigateToChatView(String aliasId);

    void navigateToSignUpView();

    void showJoinChatError();

    void showGetListError();

    void showLogoutError();
}

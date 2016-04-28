package com.tonyjhuang.cheddar.ui.onboard;

/**
 * View for onboard process.
 */
public interface OnboardView {

    void showJoinChatLoadingDialog();
    void navigateToChatView(String aliasId);
    void showJoinChatFailed();
}

package com.tonyjhuang.cheddar.ui.onboard;

/**
 * View for onboard process.
 */
public interface OnboardView {

    void showRegisterUserLoadingDialog();
    void showRegisterUserFailed();
    void showLoginUserLoadingDialog();
    void showLoginUserFailed();
    void showJoinChatLoadingDialog();
    void showJoinChatFailed();
    void navigateToListView();
    void navigateToChatView(String aliasId);
    void navigateToVerifyEmailView(String userId);
}

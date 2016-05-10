package com.tonyjhuang.cheddar.ui.onboard;

/**
 * View for onboard process.
 */
public interface OnboardView {

    void showRegisterUserLoadingDialog();
    void showRegisterUserFailed();
    void showJoinChatLoadingDialog();
    void showJoinChatFailed();
    void navigateToChatView(String aliasId);
    void navigateToVerifyEmailView(String userId);
}

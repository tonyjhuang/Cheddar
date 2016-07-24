package com.tonyjhuang.cheddar.ui.welcome;

/**
 * View for onboard process.
 */
public interface WelcomeView {

    void showRegisterUserLoadingDialog();
    void showRegisterUserFailed();
    void showRegisterUserFailed(String message);

    void showLoginUserLoadingDialog();
    void showLoginUserFailed();

    void showJoinChatLoadingDialog();
    void showJoinChatFailed();

    void navigateToListView();
    void navigateToChatView(String aliasId);
    void navigateToVerifyEmailView(String userId);

    void showNetworkConnectionError();
}

package com.tonyjhuang.cheddar.ui.login;

/**
 * What to show the user while waiting for them to
 * verify their email via email.
 */
public interface VerifyEmailView {
    void navigateToChatView(String aliasId);

    void navigateToSignupView();

    void showEmailNotVerified();

    void showResentEmailMessage(String message);

    void showResendEmailFailed();

    void showJoiningChatRoomLoading();

    void showJoiningChatRoomFailed();
}

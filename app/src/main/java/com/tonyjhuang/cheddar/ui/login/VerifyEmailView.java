package com.tonyjhuang.cheddar.ui.login;

/**
 * What to show the user while waiting for them to
 * verify their email via email.
 */
public interface VerifyEmailView {
    void navigateToListView();

    void showEmailNotVerified();

    void navigateToSignupView();

    void showResentEmailMessage(String message);

    void showResendEmailFailed();
}

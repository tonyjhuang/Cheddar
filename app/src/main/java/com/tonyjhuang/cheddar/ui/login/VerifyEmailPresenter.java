package com.tonyjhuang.cheddar.ui.login;

import com.tonyjhuang.cheddar.ui.presenter.Presenter;

/**
 * Presents to a VerifyEmailView.
 */
public interface VerifyEmailPresenter extends Presenter<VerifyEmailView> {

    void checkEmailVerification();

    void resendVerificationEmail();

    void logout();

    void onResume();
    void onPause();
    void onDestroy();
}

package com.tonyjhuang.cheddar.ui.login;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.list.ChatRoomListActivity_;
import com.tonyjhuang.cheddar.ui.onboard.OnboardActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_verify_email)
public class VerifyEmailActivity extends CheddarActivity implements VerifyEmailView {

    @Bean(VerifyEmailPresenterImpl.class)
    VerifyEmailPresenter presenter;

    @AfterInject
    public void afterInject() {
        presenter.setView(this);
    }

    @Click(R.id.check_verification)
    public void onCheckVerificationClicked() {
        presenter.checkEmailVerification();
    }

    @Click(R.id.resend_email)
    public void onResendEmailCicked() {
        presenter.resendVerificationEmail();
    }

    @Override
    public void showResentEmailMessage(String message) {
        showToast(message);
    }

    @Override
    public void showResendEmailFailed() {
        showToast(R.string.verify_email_error_resend_failed);
    }

    @Click(R.id.go_to_list)
    public void onGoToListClicked() {
        navigateToListView();
    }

    @Override
    public void navigateToListView() {
        ChatRoomListActivity_.intent(this).start();
    }

    @Override
    public void navigateToSignupView() {
        OnboardActivity_.intent(this).start();
    }

    @Override
    public void showEmailNotVerified() {
        showToast(R.string.verify_email_not_verified);
    }

    @Click(R.id.logout)
    public void onLogoutClicked() {
        presenter.logout();
    }

    @Override
    protected void onResume() {
        presenter.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        presenter.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }
}

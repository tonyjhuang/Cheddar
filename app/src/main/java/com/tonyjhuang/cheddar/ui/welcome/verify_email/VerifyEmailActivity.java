package com.tonyjhuang.cheddar.ui.welcome.verify_email;

import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.dialog.LoadingDialog;
import com.tonyjhuang.cheddar.ui.list.ChatRoomListActivity_;
import com.tonyjhuang.cheddar.ui.welcome.WelcomeActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_verify_email)
public class VerifyEmailActivity extends CheddarActivity implements VerifyEmailView {


    @Bean(VerifyEmailPresenterImpl.class)
    VerifyEmailPresenter presenter;

    private LoadingDialog loadingDialog;

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
    public void showJoiningChatRoomLoading() {
        loadingDialog = LoadingDialog.show(this, R.string.chat_join_chat);
    }

    @Override
    public void showJoiningChatRoomFailed() {
        if(loadingDialog != null) loadingDialog.dismiss();
        showToast(R.string.onboard_error_join_chat);
    }

    @Override
    public void navigateToChatView(String aliasId) {
        Intent chatIntent = ChatActivity_.intent(this).aliasId(aliasId).get();
        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(chatIntent)
                .startActivities();
    }

    public void navigateToListView() {
        ChatRoomListActivity_.intent(this).start();
    }

    @Override
    public void navigateToSignupView() {
        WelcomeActivity_.intent(this).start();
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

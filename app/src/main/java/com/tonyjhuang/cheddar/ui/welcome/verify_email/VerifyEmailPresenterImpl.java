package com.tonyjhuang.cheddar.ui.welcome.verify_email;

import android.content.Context;

import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.utils.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import rx.Subscription;
import timber.log.Timber;

@EBean
public class VerifyEmailPresenterImpl implements VerifyEmailPresenter {

    @RootContext
    Context context;

    @Pref
    CheddarPrefs_ prefs;

    @Bean
    CheddarApi api;

    /**
     * Subscription for checking email verification status.
     */
    private Subscription userEmailVerificationSubscription;

    /**
     * Subscription for resending verification email;
     */
    private Subscription resendVerificationEmailSubscription;

    /**
     * Subscription for joining a new ChatRoom.
     */
    private Subscription joinGroupChatSubscription;

    private VerifyEmailView view;

    @Override
    public void setView(VerifyEmailView view) {
        this.view = view;
    }

    @Override
    public void checkEmailVerification() {
        Timber.d("check: %b", isSubscribed(userEmailVerificationSubscription));
        // Don't attempt to check email verification status if we're already trying to
        if (isSubscribed(userEmailVerificationSubscription)) return;

        userEmailVerificationSubscription = api.isCurrentUserEmailVerified()
                .compose(Scheduler.defaultSchedulers())
                .subscribe(emailVerified -> {
                    if (emailVerified) {
                        joinChatRoom();
                    } else {
                        if (view != null) view.showEmailNotVerified();
                    }
                }, error -> Timber.e(error, "couldn't check verification status"));
    }

    private void joinChatRoom() {
        if(view != null) view.showJoiningChatRoomLoading();
        joinGroupChatSubscription = api.joinGroupChatRoom()
                .compose(Scheduler.defaultSchedulers())
                .map(Alias::objectId)
                .subscribe(aliasId -> {
                    Timber.v("joined chatroom. alias: %s", aliasId);
                    if(view != null) view.navigateToChatView(aliasId);
                }, error -> {
                    Timber.e(error, "couldn't join chatroom?");
                    if(view != null) view.showJoiningChatRoomFailed();
                });
    }

    @Override
    public void resendVerificationEmail() {
        // Don't attempt to resend verification email if we're already trying to.
        if (isSubscribed(resendVerificationEmailSubscription)) return;

        resendVerificationEmailSubscription = api.resendCurrentUserVerificationEmail()
                .compose(Scheduler.defaultSchedulers())
                .subscribe(user -> {
                    String resentEmailMessage = context.getString(R.string.verify_email_resent, user.username());
                    if (view != null) view.showResentEmailMessage(resentEmailMessage);
                }, error -> {
                    Timber.e(error, "failed to resend verification email");
                    if (view != null) view.showResendEmailFailed();
                });
    }

    @Override
    public void logout() {
        api.logoutCurrentUser().subscribe(aVoid -> {
            if (view != null) view.navigateToSignupView();
        }, error -> Timber.e(error, "couldnt logout"));
    }

    @Override
    public void onResume() {
        checkEmailVerification();
    }

    @Override
    public void onPause() {
        unsubscribe(userEmailVerificationSubscription);
        unsubscribe(resendVerificationEmailSubscription);
        unsubscribe(joinGroupChatSubscription);
    }

    @Override
    public void onDestroy() {
        view = null;
        unsubscribe(userEmailVerificationSubscription);
        unsubscribe(resendVerificationEmailSubscription);
        unsubscribe(joinGroupChatSubscription);
    }

    private boolean isSubscribed(Subscription subscription) {
        return subscription != null && !subscription.isUnsubscribed();
    }

    private void unsubscribe(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}

package com.tonyjhuang.cheddar.ui.onboard;

import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetrics;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.ui.login.RegisterFragment;
import com.tonyjhuang.cheddar.utils.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import rx.Subscription;
import rx.subjects.AsyncSubject;
import timber.log.Timber;

/**
 * Presents to our OnboardView.
 */
@EBean
public class OnboardPresenterImpl implements OnboardPresenter {

    @Bean
    CheddarApi api;

    @Pref
    CheddarPrefs_ prefs;


    /**
     * Use an AsyncSubject here so that if the network call returns
     * while the view is paused, we can return the cached value on resume.
     */
    private Subscription registerUserCachedSubscription;
    private AsyncSubject<User> registerUserSubject;
    private Subscription registerUserSubscription;

    /**
     * The view we're presenting to.
     */
    private OnboardView view;

    @Override
    public void setView(OnboardView view) {
        this.view = view;
    }

    @Subscribe
    public void onJoinChatEvent(OnboardActivity.AlphaWarningFragment.JoinChatEvent event) {
        if(view != null) view.showJoinChatLoadingDialog();
        api.joinGroupChatRoom().compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    CheddarMetrics.trackJoinChatRoom(alias.chatRoomId());
                    prefs.onboardShown().put(true);
                    if(view != null) view.navigateToChatView(alias.objectId());
                }, error -> {
                    if(view != null) view.showJoinChatFailed();
                });
    }

    @Subscribe
    public void onRegisterUserRequestEvent(RegisterFragment.RegisterUserRequestEvent event) {
        if(registerUserCachedSubscription != null && !registerUserCachedSubscription.isUnsubscribed()) {
            // Don't allow multiple registrations.
            return;
        }

        if(view != null) view.showRegisterUserLoadingDialog();

        registerUserSubject = AsyncSubject.create();
        registerUserCachedSubscription = api.registerNewUser(event.email, event.password)
                .compose(Scheduler.backgroundSchedulers())
                .subscribe(registerUserSubject);
        subscribeToRegisterUserSubject();
    }

    /**
     * Subscribe to our register user AsyncSubject.
     */
    private void subscribeToRegisterUserSubject() {
        registerUserSubscription = registerUserSubject
                .compose(Scheduler.defaultSchedulers())
                .subscribe(user -> {
                    unsubscribe(registerUserCachedSubscription);
                    prefs.onboardShown().put(true);
                    if(view != null) view.navigateToVerifyEmailView(user.objectId());
                }, error -> {
                    Timber.e(error, "failed to register user");
                    if(view != null) view.showRegisterUserFailed();
                    unsubscribe(registerUserCachedSubscription);
                });
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        if(registerUserCachedSubscription != null &&
                !registerUserCachedSubscription.isUnsubscribed()) {
            subscribeToRegisterUserSubject();
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        unsubscribe(registerUserSubscription);
    }

    @Override
    public void onDestroy() {
        unsubscribe(registerUserCachedSubscription);
        unsubscribe(registerUserSubscription);
        view = null;
    }

    private void unsubscribe(Subscription subscription) {
        if(subscription != null) {
            subscription.unsubscribe();
        }
    }
}

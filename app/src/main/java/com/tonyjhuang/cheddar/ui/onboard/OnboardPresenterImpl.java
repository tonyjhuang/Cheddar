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
     * Use an AsyncSubject to cache the result of the register network call.
     */
    private Subscription registerUserSubjectSubscription;
    private AsyncSubject<User> registerUserSubject;
    private Subscription registerUserSubscription;

    /**
     * Use an AsyncSubject to cache the result of the login network call.
     */
    private Subscription loginUserSubjectSubscription;
    private AsyncSubject<User> loginUserSubject;
    private Subscription loginUserSubscription;

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
        if (view != null) view.showJoinChatLoadingDialog();
        api.joinGroupChatRoom().compose(Scheduler.defaultSchedulers())
                .subscribe(alias -> {
                    CheddarMetrics.trackJoinChatRoom(alias.chatRoomId());
                    prefs.onboardShown().put(true);
                    if (view != null) view.navigateToChatView(alias.objectId());
                }, error -> {
                    if (view != null) view.showJoinChatFailed();
                });
    }

    @Subscribe
    public void onRegisterUserRequestEvent(RegisterFragment.RegisterUserRequestEvent event) {
        if (isSubscribed(loginUserSubscription) || isSubscribed(registerUserSubscription)) return;

        if (view != null) view.showRegisterUserLoadingDialog();

        registerUserSubject = AsyncSubject.create();
        registerUserSubjectSubscription = api.registerNewUser(event.email, event.password)
                .compose(Scheduler.backgroundSchedulers())
                .subscribe(registerUserSubject);

        subscribeToRegisterUserSubject();
    }

    /**
     * Subscribe to our register user AsyncSubject.
     */
    private void subscribeToRegisterUserSubject() {
        if (registerUserSubject == null) return;
        registerUserSubscription = registerUserSubject
                .compose(Scheduler.defaultSchedulers())
                .subscribe(user -> {
                    unsubscribe(registerUserSubjectSubscription);
                    prefs.currentUserId().put(user.objectId());
                    prefs.onboardShown().put(true);
                    if (view != null) view.navigateToVerifyEmailView(user.objectId());
                }, error -> {
                    Timber.e(error, "failed to register user");
                    if (view != null) view.showRegisterUserFailed();
                    unsubscribe(registerUserSubjectSubscription);
                });
    }

    @Subscribe
    public void onLoginUserRequestEvent(RegisterFragment.LoginUserRequestEvent event) {
        if (isSubscribed(loginUserSubscription) || isSubscribed(registerUserSubscription)) return;

        if (view != null) view.showLoginUserLoadingDialog();

        loginUserSubject = AsyncSubject.create();
        loginUserSubjectSubscription = api.login(event.email, event.password)
                .compose(Scheduler.backgroundSchedulers())
                .subscribe(loginUserSubject);

        subscribeToLoginUserSubject();
    }

    /**
     * Subscribe to our login user AsyncSubject.
     */
    private void subscribeToLoginUserSubject() {
        if (loginUserSubject == null) return;
        loginUserSubscription = loginUserSubject
                .compose(Scheduler.defaultSchedulers())
                .subscribe(user -> {
                    Timber.d("user: " + user);
                    unsubscribe(loginUserSubjectSubscription);
                    prefs.currentUserId().put(user.objectId());
                    prefs.onboardShown().put(true);
                    if (view != null) view.navigateToListView();
                }, error -> {
                    Timber.e(error, "failed to login");
                    if (view != null) view.showLoginUserFailed();
                    unsubscribe(loginUserSubjectSubscription);
                });
    }

    @Override
    public void onResume() {
        EventBus.getDefault().register(this);
        if (isSubscribed(registerUserSubjectSubscription)) {
            subscribeToRegisterUserSubject();
        } else if (isSubscribed(loginUserSubjectSubscription)) {
            subscribeToLoginUserSubject();
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        unsubscribe(registerUserSubscription);
        unsubscribe(loginUserSubscription);
    }

    @Override
    public void onDestroy() {
        unsubscribe(registerUserSubjectSubscription);
        unsubscribe(loginUserSubjectSubscription);
        view = null;
    }

    private void unsubscribe(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private boolean isSubscribed(Subscription subscription) {
        return subscription != null && !subscription.isUnsubscribed();
    }
}

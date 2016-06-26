package com.tonyjhuang.cheddar.ui.welcome;

import android.content.Context;

import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.background.ConnectivityBroadcastReceiver;
import com.tonyjhuang.cheddar.utils.Scheduler;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import rx.Subscription;
import rx.subjects.AsyncSubject;
import timber.log.Timber;

/**
 * Presents to our WelcomeView.
 */
@EBean
public class WelcomePresenterImpl implements WelcomePresenter {

    @RootContext
    Context context;

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
    private WelcomeView view;

    @Override
    public void setView(WelcomeView view) {
        this.view = view;
    }

    @Subscribe
    public void onRequestRegisterEvent(WelcomeFragment.RequestRegisterEvent event) {
        if (isSubscribed(loginUserSubscription) || isSubscribed(registerUserSubscription)) return;

        if(!ConnectivityBroadcastReceiver.isConnected(context)) {
            if(view != null) view.showNetworkConnectionError();
            return;
        }

        if (view != null) view.showRegisterUserLoadingDialog();

        registerUserSubject = AsyncSubject.create();
        registerUserSubjectSubscription = api.registerNewUser(event.username, event.password)
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
    public void onRequestLoginEvent(WelcomeFragment.RequestLoginEvent event) {
        if (isSubscribed(loginUserSubscription) || isSubscribed(registerUserSubscription)) return;

        if(!ConnectivityBroadcastReceiver.isConnected(context)) {
            if(view != null) view.showNetworkConnectionError();
            return;
        }

        if (view != null) view.showLoginUserLoadingDialog();

        loginUserSubject = AsyncSubject.create();
        loginUserSubjectSubscription = api.login(event.username, event.password)
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
                    unsubscribe(loginUserSubjectSubscription);
                    prefs.currentUserId().put(user.objectId());
                    prefs.onboardShown().put(true);
                    if (view != null) {
                        Timber.d("logged in user: " + user);
                        if(user.emailVerified()) {
                            view.navigateToListView();
                        } else {
                            view.navigateToVerifyEmailView(user.objectId());
                        }
                    }
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

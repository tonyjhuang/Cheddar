package com.tonyjhuang.cheddar;

import com.tonyjhuang.cheddar.ui.list.ChatRoomListActivity_;
import com.tonyjhuang.cheddar.ui.login.VerifyEmailActivity_;
import com.tonyjhuang.cheddar.ui.onboard.OnboardActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.sharedpreferences.Pref;

import timber.log.Timber;

/**
 * Decides what default activity to start when Cheddar is opened.
 */
@EActivity
public class AppRouter extends CheddarActivity {
    @Pref
    CheddarPrefs_ prefs;

    @AfterInject
    public void start() {
        boolean onboardShown = prefs.onboardShown().getOr(false);
        String currentUserId = prefs.currentUserId().getOr("");
        Timber.d("currentUserId: %s", currentUserId);
        if (!onboardShown || currentUserId.isEmpty()) {
            OnboardActivity_.intent(this).start();
        } else {
            boolean emailVerified = prefs.userEmailVerified().getOr(false);
            if(!emailVerified) {
                VerifyEmailActivity_.intent(this).start();
            } else {
                ChatRoomListActivity_.intent(this).start();
            }
        }
    }
}

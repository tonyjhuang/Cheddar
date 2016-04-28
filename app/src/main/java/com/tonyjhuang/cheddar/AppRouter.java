package com.tonyjhuang.cheddar;

import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.list.ChatRoomListActivity_;
import com.tonyjhuang.cheddar.ui.main.MainActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.sharedpreferences.Pref;

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
        if (!onboardShown) {
            MainActivity_.intent(this).start();
        } else {
            String lastOpenedAlias = prefs.lastOpenedAlias().getOr("");
            if (!lastOpenedAlias.isEmpty()) {
                ChatActivity_.intent(this).aliasId(lastOpenedAlias).start();
            } else {
                ChatRoomListActivity_.intent(this).start();
            }
        }
    }
}

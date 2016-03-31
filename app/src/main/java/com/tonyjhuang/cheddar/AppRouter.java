package com.tonyjhuang.cheddar;

import android.util.Log;

import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
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
        String lastOpenedAlias = prefs.lastOpenedAlias().get();
        Log.e("AppRouter", "lastOpened: " + lastOpenedAlias);
        if (lastOpenedAlias == null || lastOpenedAlias.isEmpty()) {
            MainActivity_.intent(this).start();
        } else {
            ChatActivity_.intent(this).aliasId(lastOpenedAlias).start();
        }
    }
}

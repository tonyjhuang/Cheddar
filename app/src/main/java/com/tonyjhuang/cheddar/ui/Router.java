package com.tonyjhuang.cheddar.ui;

import android.util.Log;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.main.MainActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by tonyjhuang on 3/4/16.
 */
@EActivity
public class Router extends CheddarActivity {
    @Pref
    CheddarPrefs_ prefs;

    @AfterInject
    public void decideActivity() {
        String activeAlias = prefs.activeAlias().get();
        Log.e("Router", "activeAlias: " + activeAlias);
        if(activeAlias == null || activeAlias.isEmpty()) {
            MainActivity_.intent(this).start();
        } else {
            ChatActivity_.intent(this).aliasId(activeAlias).start();
        }
    }
}

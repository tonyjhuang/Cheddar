package com.tonyjhuang.cheddar;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface CheddarPrefs {

    String gcmRegistrationToken();

    String currentUserId();

    boolean userEmailVerified();

    boolean onboardShown();

    String unreadMessages();

    String pushChannels();

    String lastVersionName();
}

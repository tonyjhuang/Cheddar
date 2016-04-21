package com.tonyjhuang.cheddar;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface CheddarPrefs {

    String gcmRegistrationToken();

    String activeAlias();

    String currentUser();

    String lastOpenedAlias();

    String unreadMessages();

    String pushChannels();

    String lastVersionName();
}

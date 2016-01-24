package com.tonyjhuang.chatly;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface CheddarPrefs {

    @DefaultString("")
    String emailAddress();
}

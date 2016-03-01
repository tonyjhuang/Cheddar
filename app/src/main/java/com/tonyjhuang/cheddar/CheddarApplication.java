package com.tonyjhuang.cheddar;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.api.models.ChatRoom;
import com.tonyjhuang.cheddar.api.models.Message;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by tonyjhuang on 1/23/16.
 */
public class CheddarApplication extends Application{

    public static final boolean DEBUG = false;

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Effra-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(ChatRoom.class);
        ParseObject.registerSubclass(Alias.class);
        Parse.initialize(this);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}

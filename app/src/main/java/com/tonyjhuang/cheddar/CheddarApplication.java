package com.tonyjhuang.cheddar;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.tonyjhuang.cheddar.api.models.parse.ParseAlias;
import com.tonyjhuang.cheddar.api.models.parse.ParseChatEvent;
import com.tonyjhuang.cheddar.api.models.parse.ParseChatRoom;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by tonyjhuang on 1/23/16.
 */
public class CheddarApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Timber.plant(new Timber.DebugTree());

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("Effra-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        ParseObject.registerSubclass(ParseChatEvent.class);
        ParseObject.registerSubclass(ParseChatRoom.class);
        ParseObject.registerSubclass(ParseAlias.class);
        Parse.initialize(this);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}

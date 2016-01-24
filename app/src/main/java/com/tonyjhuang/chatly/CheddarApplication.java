package com.tonyjhuang.chatly;

import android.app.Application;

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
                        .setDefaultFontPath("FuturaLT-Book.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }
}

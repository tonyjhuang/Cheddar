package com.tonyjhuang.cheddar;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseInstallation;

import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by tonyjhuang on 1/23/16.
 */
public class CheddarApplication extends MultiDexApplication {
    private static final Pattern ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$");

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Timber.plant(new Timber.DebugTree() {

            private String method(StackTraceElement element) {
                String method = element.getMethodName();
                int first = method.indexOf("$");
                int second = method.indexOf("$", first + 1);
                if (first != -1 && second != -1) {
                    return method.substring(first + 1, second);
                } else {
                    return method;
                }
            }

            private String filePointer(StackTraceElement element) {
                return element.getFileName() + ":" + element.getLineNumber();
            }

            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return String.format("^(%s)%s", filePointer(element), method(element));
            }
        });

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("Effra-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        Parse.initialize(this);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
    }
}

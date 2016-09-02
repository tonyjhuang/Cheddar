package com.tonyjhuang.cheddar.utils;

import com.crashlytics.android.Crashlytics;
import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.api.network.ParseApi;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import rx.Observable;

/**
 * Helper for checking if we need an app update.
 */
@EBean
public class VersionChecker {
    @Bean
    ParseApi parseApi;

    public Observable<Boolean> check() {
        return parseApi.getMinimumBuildNumber()
                .map(minBuildNumber -> BuildConfig.VERSION_CODE < minBuildNumber)
                .doOnError(Crashlytics::logException);
    }
}

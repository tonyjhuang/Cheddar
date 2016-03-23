package com.tonyjhuang.cheddar.presenter;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjhuang on 3/18/16.
 */
public class Scheduler {
    public static <T> Observable.Transformer<T, T> defaultSchedulers() {
        return (o) -> o.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
    public static <T> Observable.Transformer<T, T> backgroundSchedulers() {
        return (o) -> o.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }
}

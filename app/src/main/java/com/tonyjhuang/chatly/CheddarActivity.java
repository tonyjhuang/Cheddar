package com.tonyjhuang.chatly;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CheddarActivity extends AppCompatActivity {

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onPause() {
        subscriptions.unsubscribe();
        super.onPause();
    }

    protected <T> void subscribe(Observable<T> observable, Action1<T> onNext) {
        subscribe(observable, onNext, (throwable) -> Log.e(getClass().getSimpleName(), throwable.toString()));
    }

    protected <T> void subscribe(Observable<T> observable, Action1<T> onNext, Action1<Throwable> onError) {
        subscribe(observable, onNext, onError, () -> {});
    }

    protected <T> void subscribe(Observable<T> observable, Action1<T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        subscriptions.add(observable.compose(applySchedulers()).subscribe(onNext, onError, onCompleted));
    }

    private <T> Observable.Transformer<T, T> applySchedulers() {
        return (o) -> o.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}

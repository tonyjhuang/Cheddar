package com.tonyjhuang.cheddar;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import org.androidannotations.annotations.EActivity;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@EActivity
public class CheddarActivity extends RxAppCompatActivity {

    private static final String TAG = CheddarActivity.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected <T> Subscription subscribe(Observable<T> observable, Action1<T> onNext) {
        return subscribe(observable, onNext,
                throwable -> Log.e(getClass().getSimpleName(), throwable.toString()));
    }

    protected <T> Subscription subscribe(Observable<T> observable, Action1<T> onNext, Action1<Throwable> onError) {
        return subscribe(observable, onNext, onError, () -> {
        });
    }

    protected <T> Subscription subscribe(Observable<T> observable, Action1<T> onNext, Action1<Throwable> onError, Action0 onCompleted) {
        return observable.compose(applySchedulers())
                .compose(bindUntilEvent(ActivityEvent.STOP))
                .subscribe(onNext, onError, onCompleted);
    }

    private <T> Observable.Transformer<T, T> applySchedulers() {
        return (o) -> o.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    protected void showToast(int stringRes) {
        showToast(getString(stringRes));
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

package com.tonyjhuang.cheddar;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.tonyjhuang.cheddar.service.PushRegistrationIntentService;
import com.tonyjhuang.cheddar.service.PushRegistrationIntentService_;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import org.androidannotations.annotations.EActivity;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@EActivity
public class CheddarActivity extends RxAppCompatActivity {

    private static final String TAG = CheddarActivity.class.getSimpleName();

    private Subscriber<? super String> gcmRegistrationTokenSubscriber;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    protected Observable<String> getGcmRegistrationToken() {
        return Observable.create((subscriber -> {
            gcmRegistrationTokenSubscriber = subscriber;
            PushRegistrationIntentService_.intent(this).registerForPush().start();
        }));
    }

    public void onEvent(PushRegistrationIntentService.RegistrationCompletedEvent event) {
        Log.d(TAG, "register completed");
        if (gcmRegistrationTokenSubscriber != null) {
            if (event.token != null) {
                gcmRegistrationTokenSubscriber.onNext(event.token);
                gcmRegistrationTokenSubscriber.onCompleted();
            } else {
                gcmRegistrationTokenSubscriber.onError(new Exception("Failed to register Instance ID"));

            }
        }
    }

    protected <T> Subscription subscribe(Observable<T> observable, Action1<T> onNext) {
        return subscribe(observable, onNext,
                throwable -> Log.e(getClass().getSimpleName(), throwable.toString()));
    }

    protected <T> Subscription subscribe(Observable<T> observable, Action1<T> onNext, Action1<Throwable> onError) {
        return subscribe(observable, onNext, onError, () -> {});
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

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    protected boolean checkPlayServices() {
        Log.d(TAG, "checkPlayServices");
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Log.d(TAG, "checkPlayServices - show dialog");
                apiAvailability.getErrorDialog(this, resultCode, 0).show();
            } else {
                Log.d(TAG, "checkPlayServices - unsupported");
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        Log.d(TAG, "checkPlayServices - connected");
        return true;
    }

    protected void showToast(int stringRes) {
        showToast(getString(stringRes));
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

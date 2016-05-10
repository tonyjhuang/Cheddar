package com.tonyjhuang.cheddar;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.background.ConnectivityBroadcastReceiver;
import com.tonyjhuang.cheddar.ui.onboard.OnboardActivity_;
import com.tonyjhuang.cheddar.utils.Scheduler;
import com.tonyjhuang.cheddar.ui.dialog.ChangelogDialog;

import org.androidannotations.annotations.EActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@EActivity(R.layout.activity_main)
public abstract class CheddarActivity extends AppCompatActivity {

    private static final String TAG = CheddarActivity.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void showChangeLog(CheddarPrefs_ prefs) {
        String lastVersionName = prefs.lastVersionName().get();
        String versionName = getVersionName();
        if ((!versionName.isEmpty() && !versionName.equals(lastVersionName))) {
            ChangelogDialog.show(this);
            prefs.lastVersionName().put(versionName);
        }
    }

    protected void checkCurrentUser(CheddarApi api) {
        if (ConnectivityBroadcastReceiver.getLastKnownConnected()) {
            api.getCurrentUser().compose(Scheduler.defaultSchedulers())
                    .doOnError(error -> {
                        api.debugReset().publish().connect();
                        navigateToOnboardView();
                    })
                    .publish().connect();
        }
    }

    protected void navigateToOnboardView() {
        OnboardActivity_.intent(this).start();
        finish();
    }

    /**
     * Returns the version name or the empty string if it can't be retrieved.
     */
    protected String getVersionName() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            Log.d(TAG, pInfo.versionName);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Couldn't retrieve package name: " + e.toString());
            return "";
        }
    }

    protected void showToast(int stringRes) {
        showToast(getString(stringRes));
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

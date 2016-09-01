package com.tonyjhuang.cheddar;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tonyjhuang.cheddar.ui.dialog.ChangelogDialog;
import com.tonyjhuang.cheddar.ui.dialog.ForceVersionUpdateDialog;
import com.tonyjhuang.cheddar.utils.Scheduler;
import com.tonyjhuang.cheddar.utils.VersionChecker;

import org.androidannotations.annotations.EActivity;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@EActivity
public abstract class CheddarActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void showChangeLog(CheddarPrefs_ prefs) {
        String lastVersionName = prefs.lastVersionName().get();
        if (!BuildConfig.VERSION_NAME.equals(lastVersionName)) {
            ChangelogDialog.show(this);
            prefs.lastVersionName().put(BuildConfig.VERSION_NAME);
        }
    }

    protected void checkForUpdate(VersionChecker versionChecker) {
        versionChecker.check().compose(Scheduler.defaultSchedulers())
                .subscribe(shouldUpgrade -> {
                    if (shouldUpgrade) {
                        ForceVersionUpdateDialog.show(this);
                    }
                }, e -> Timber.e(e, "Couldn't check for upgrade"));
    }

    protected void showToast(int stringRes) {
        showToast(getString(stringRes));
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

package com.tonyjhuang.cheddar;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.tonyjhuang.cheddar.ui.dialog.ChangelogDialog;

import org.androidannotations.annotations.EActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@EActivity(R.layout.activity_welcome)
public abstract class CheddarActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void showChangeLog(CheddarPrefs_ prefs) {
        String lastVersionName = prefs.lastVersionName().get();
        if (!BuildConfig.VERSION_NAME.equals(lastVersionName)){
            ChangelogDialog.show(this);
            prefs.lastVersionName().put(BuildConfig.VERSION_NAME);
        }
    }

    protected void showToast(int stringRes) {
        showToast(getString(stringRes));
    }

    protected void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}

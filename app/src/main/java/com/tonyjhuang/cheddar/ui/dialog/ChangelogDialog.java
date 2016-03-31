package com.tonyjhuang.cheddar.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.tonyjhuang.cheddar.R;

/**
 * Created by tonyjhuang on 3/31/16.
 */
public class ChangelogDialog extends AlertDialog {

    public static ChangelogDialog show(Context context) {
        ChangelogDialog changelogDialog = new ChangelogDialog(context, true, null);
        changelogDialog.show();
        return changelogDialog;
    }


    protected ChangelogDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_changelog);
    }
}

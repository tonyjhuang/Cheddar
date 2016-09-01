package com.tonyjhuang.cheddar.ui.dialog;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.R;

/**
 * Undismissable dialog that prompts a user to update the app.
 */
public class ForceVersionUpdateDialog {
    public static void show(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.force_update_title)
                .setMessage(R.string.force_update_desc)
                .setPositiveButton(R.string.force_update_confirm, (DialogInterface dialog, int which) -> {
                    if (BuildConfig.BUILD_TYPE.equals("debug")) {
                        viewAppPage(context, "com.tonyjhuang.cheddar.beta");
                    } else {
                        viewAppPage(context, context.getPackageName());
                    }
                });

        AlertDialog dialog = builder.show();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
    }

    private static void viewAppPage(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        try {
            context.startActivity(intent);
        } catch(ActivityNotFoundException e) {
            intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=" + packageName));
            context.startActivity(intent);
        }
    }
}

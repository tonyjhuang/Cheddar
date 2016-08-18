package com.tonyjhuang.cheddar.ui.welcome;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.tonyjhuang.cheddar.R;

public class ResetPasswordDialog {
    public static void getEmail(Context context, Callback callback) {
        View view = View.inflate(context, R.layout.dialog_reset_password, null);
        EditText emailView = (EditText) view.findViewById(R.id.email);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.common_submit, (dialog, which) -> {
                    callback.onEmailReceived(emailView.getText().toString());
                })
                .setNegativeButton(R.string.common_cancel, null);
        builder.show();
    }

    public interface Callback {
        void onEmailReceived(String email);
    }
}

package com.tonyjhuang.cheddar.ui.welcome;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.tonyjhuang.cheddar.R;

public class RegistrationCodeDialog {
    public static void getRegistrationCode(Context context, Callback callback) {
        View view = View.inflate(context, R.layout.dialog_registration_code, null);
        EditText registrationCodeView = (EditText) view.findViewById(R.id.registration_code);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.common_submit, (dialog, which) -> {
                    callback.onRegistrationCodeReceived(registrationCodeView.getText().toString());
                })
                .setNegativeButton(R.string.common_cancel, null);
        builder.show();
    }

    public interface Callback {
        void onRegistrationCodeReceived(String registrationCode);
    }
}

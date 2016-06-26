package com.tonyjhuang.cheddar.ui.welcome;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tonyjhuang.cheddar.R;

import java.util.regex.Pattern;

public class DifferentSchoolDialog {
    public static void getSchoolAndEmail(Context context, Callback callback) {
        View view = View.inflate(context, R.layout.dialog_different_school, null);
        EditText schoolView = (EditText) view.findViewById(R.id.school);
        EditText emailView = (EditText) view.findViewById(R.id.email);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton(R.string.common_submit, null)
                .setNegativeButton(R.string.common_cancel, null);
        AlertDialog dialog = builder.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
            String school = schoolView.getText().toString();
            String email = emailView.getText().toString();
            SchoolAndEmailValidator validator = new SchoolAndEmailValidator();
            if (validator.validate(context, school, email)) {
                callback.onSchoolAndEmailReceived(school, email);
                dialog.dismiss();
            }
        });
    }

    public interface Callback {
        void onSchoolAndEmailReceived(String school, String email);
    }

    private static class SchoolAndEmailValidator {

        // regexs adapted from
        // http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
        private static final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        private final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

        public boolean validate(Context context, String school, String email) {
            if (school.isEmpty()) {
                showToast(context, R.string.different_school_error_school);
                return false;
            } else if (email.isEmpty()) {
                showToast(context, R.string.different_school_error_email_missing);
                return false;
            } else if (!emailPattern.matcher(email).matches()) {
                showToast(context, R.string.different_school_error_email_invalid);
                return false;
            }
            return true;
        }

        private void showToast(Context context, int stringResId) {
            Toast.makeText(context, stringResId, Toast.LENGTH_SHORT).show();
        }
    }
}

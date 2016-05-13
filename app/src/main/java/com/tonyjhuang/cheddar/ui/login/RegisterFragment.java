package com.tonyjhuang.cheddar.ui.login;

import android.support.v4.app.Fragment;
import android.widget.EditText;
import android.widget.Toast;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

@EFragment(R.layout.fragment_register)
public class RegisterFragment extends Fragment {

    /**
     * DEBUG emails to test with
     */
    private static final String DEBUG_EMAIL_1 = "huang.to@husky.neu.edu";
    private static final String DEBUG_EMAIL_2 = "tony.huang.jun@gmail.com";

    // regex adapted from
    // http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
    private static final String EMAIL_PATTERN =
            "^(([_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@husky.neu.edu)|(tony.huang.jun@gmail.com))$";
    /**
     * Verifies husky email addresses (...@husky.neu.edu)
     */
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

    @ViewById(R.id.email)
    EditText emailView;
    @ViewById(R.id.password)
    EditText passwordView;

    @Click(R.id.register)
    public void onRegisterClicked() {
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        if(!validateFields(email, password)) return;

        EventBus.getDefault().post(new RegisterUserRequestEvent(email, password));
    }

    @Click(R.id.login)
    public void onLoginClicked() {
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        if(!validateFields(email, password)) return;

        EventBus.getDefault().post(new LoginUserRequestEvent(email, password));
    }

    /**
     * Test email and password for validity, display errors if necessary,
     * returns true if both fields are valid.
     */
    private boolean validateFields(String email, String password) {
        if (!emailPattern.matcher(email).matches()) {
            showToast(R.string.signup_error_email_invalid);
            return false;
        }

        if (password.length() < 6) {
            showToast(R.string.signup_error_password_invalid);
            return false;
        }

        return true;
    }

    @Click(R.id.debug_email)
    public void onDebugEmailClicked() {
        if (!emailView.getText().toString().equals(DEBUG_EMAIL_1)) {
            emailView.setText(DEBUG_EMAIL_1);
        } else {
            if (emailView.getText().toString().equals(DEBUG_EMAIL_2)) {
                emailView.setText(DEBUG_EMAIL_1);
            } else {
                emailView.setText(DEBUG_EMAIL_2);
            }
        }
    }

    private void showToast(int stringRes) {
        Toast.makeText(getContext(), stringRes, Toast.LENGTH_SHORT).show();
    }

    public static class LoginUserRequestEvent {
        public String email;
        public String password;

        public LoginUserRequestEvent(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class RegisterUserRequestEvent {
        public String email;
        public String password;

        public RegisterUserRequestEvent(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}

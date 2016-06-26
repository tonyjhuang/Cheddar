package com.tonyjhuang.cheddar.ui.welcome;

import android.animation.Animator;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.util.regex.Pattern;

@EFragment(R.layout.fragment_welcome)
public class WelcomeFragment extends Fragment implements BackButtonHandler {

    @ViewById(R.id.welcome_layout)
    ViewGroup welcomeLayoutGroup;

    @ViewById(R.id.login_layout)
    ViewGroup loginLayoutGroup;
    @ViewById(R.id.login_email)
    EditText loginEmailView;
    @ViewById(R.id.login_password)
    EditText loginPasswordView;

    @ViewById(R.id.register_layout)
    ViewGroup registerLayoutGroup;
    @ViewById(R.id.register_email)
    EditText registerEmailView;
    @ViewById(R.id.register_password)
    EditText registerPasswordView;
    @ViewById(R.id.register_confirm_password)
    EditText registerConfirmPasswordView;


    /**
     * Applies validation logic on user credentials.
     */
    private UserCredentialValidator validator = new UserCredentialValidator();

    @Click({R.id.welcome_login, R.id.register_login})
    public void onShowLoginLayoutClicked() {
        showLayoutGroupView(LayoutGroupViewType.LOGIN);
    }

    @Click({R.id.welcome_register, R.id.login_register})
    public void onShowRegisterLayoutClicked() {
        showLayoutGroupView(LayoutGroupViewType.REGISTER);
    }

    @Click(R.id.register_different_school)
    public void onRegisterDifferentSchoolClicked() {
        showLayoutGroupView(LayoutGroupViewType.WELCOME);
        DifferentSchoolDialog.getSchoolAndEmail(getContext(), (school, email) -> {
            showToast(R.string.different_school_thanks);
            EventBus.getDefault().post(new RegisterDifferentSchoolEvent(school, email));
        });
    }

    @Click(R.id.register_register)
    public void onRegisterClicked() {
        String email = registerEmailView.getText().toString();
        String password = registerPasswordView.getText().toString();
        String password2 = registerConfirmPasswordView.getText().toString();
        if (validator.validate(email, password, password2)) {
            EventBus.getDefault().post(new RequestRegisterEvent(email, password));
        }
    }

    @Click(R.id.login_login)
    public void onLoginClicked() {
        String email = loginEmailView.getText().toString();
        String password = loginPasswordView.getText().toString();
        if (validator.validate(email, password)) {
            EventBus.getDefault().post(new RequestLoginEvent(email, password));
        }
    }

    private void showLayoutGroupView(LayoutGroupViewType type) {
        LayoutGroupViewType currentVisible = getCurrentVisibleLayoutGroupViewType();
        boolean showWelcome = type.equals(LayoutGroupViewType.WELCOME);
        boolean showLogin = type.equals(LayoutGroupViewType.LOGIN);
        boolean showRegister = type.equals(LayoutGroupViewType.REGISTER);
        possiblyDisplayViewGroup(welcomeLayoutGroup, showWelcome, currentVisible.equals(LayoutGroupViewType.WELCOME));
        possiblyDisplayViewGroup(loginLayoutGroup, showLogin, currentVisible.equals(LayoutGroupViewType.LOGIN));
        possiblyDisplayViewGroup(registerLayoutGroup, showRegister, currentVisible.equals(LayoutGroupViewType.REGISTER));
    }

    private void possiblyDisplayViewGroup(ViewGroup viewGroup, boolean shouldShow, boolean isVisible) {
        if (shouldShow || isVisible) {
            viewGroup.setVisibility(View.VISIBLE);
            viewGroup.animate().alpha(shouldShow ? 1 : 0).setStartDelay(shouldShow ? 125 : 0)
                    .setListener(new SimpleAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            viewGroup.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                            viewGroup.animate().setListener(null);
                        }
                    });
        } else {
            viewGroup.setVisibility(View.GONE);
        }
    }

    private boolean isViewGroupVisible(ViewGroup viewGroup) {
        return viewGroup != null && viewGroup.getVisibility() == View.VISIBLE;
    }

    private LayoutGroupViewType getCurrentVisibleLayoutGroupViewType() {
        if (isViewGroupVisible(registerLayoutGroup)) {
            return LayoutGroupViewType.REGISTER;
        } else if (isViewGroupVisible(loginLayoutGroup))
            return LayoutGroupViewType.LOGIN;
        else {
            return LayoutGroupViewType.WELCOME;
        }
    }

    private void showToast(int stringRes) {
        Toast.makeText(getContext(), stringRes, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean handleBackPress() {
        switch (getCurrentVisibleLayoutGroupViewType()) {
            case WELCOME:
                return false;
            default:
                showLayoutGroupView(LayoutGroupViewType.WELCOME);
                return true;
        }
    }

    private enum LayoutGroupViewType {
        WELCOME, LOGIN, REGISTER
    }

    public static class RequestLoginEvent {
        public String username;
        public String password;

        public RequestLoginEvent(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class RequestRegisterEvent {
        public String username;
        public String password;

        public RequestRegisterEvent(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class RegisterDifferentSchoolEvent {
        public String school;
        public String email;

        public RegisterDifferentSchoolEvent(String school, String email) {
            this.school = school;
            this.email = email;
        }
    }

    /**
     * Basic implementation of AnimatorListener.
     */
    private static class SimpleAnimatorListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    private class UserCredentialValidator {

        // regexs adapted from
        // http://www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression/
        private static final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        private static final String HUSKY_EMAIL_PATTERN =
                "^(([_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@husky.neu.edu)"
                        + "|(tony.huang.jun@gmail.com))$"; // DEBUG, REMOVE

        private final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
        private final Pattern huskyEmailPattern = Pattern.compile(HUSKY_EMAIL_PATTERN);

        public boolean validate(String email, String password) {
            if (!emailPattern.matcher(email).matches()) {
                showToast(R.string.welcome_error_email_invalid);
                return false;
            }
            if (!huskyEmailPattern.matcher(email).matches()) {
                showToast(R.string.welcome_error_email_nonhusky);
                return false;
            }
            if (password.length() < 6) {
                showToast(R.string.welcome_error_password_length);
                return false;
            }

            return true;
        }

        public boolean validate(String email, String password, String password2) {
            if (!validate(email, password)) {
                return false;
            }
            if (!password.equals(password2)) {
                showToast(R.string.welcome_error_mismatch_password);
                return false;
            }
            return true;
        }
    }
}

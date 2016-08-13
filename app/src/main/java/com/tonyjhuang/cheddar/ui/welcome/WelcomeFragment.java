package com.tonyjhuang.cheddar.ui.welcome;

import android.animation.Animator;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.regex.Pattern;

import timber.log.Timber;

@EFragment(R.layout.fragment_welcome)
public class WelcomeFragment extends Fragment implements BackButtonHandler, KeyboardObserver.KeyboardListener {

    private static final String HUSKY_EMAIL_PATTERN =
            "^(([_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@husky.neu.edu)"
                    + "|(tony.huang.jun@gmail.com))$"; // DEBUG, REMOVE
    private final Pattern huskyEmailPattern = Pattern.compile(HUSKY_EMAIL_PATTERN);

    @ViewById(R.id.welcome_layout)
    ViewGroup welcomeLayoutGroup;

    @ViewById(R.id.app_name)
    View appNameView;
    @ViewById(R.id.app_name_placeholder)
    View appNamePlaceholderView;
    @ViewById(R.id.tagline)
    View taglineView;

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

    @ViewsById({R.id.login_register, R.id.register_login, R.id.register_different_school})
    List<View> secondaryFormViews;

    /**
     * Is the keyboard currently shown?
     */
    boolean keyboardIsShown = false;

    /**
     * Has the appNameView been moved to its initial position?
     * If not, don't attempt to manipulate its x, y until it has.
     */
    boolean appNameViewInitialPosition = false;

    /**
     * Applies validation logic on user credentials.
     */
    private UserCredentialValidator validator = new UserCredentialValidator();

    @AfterViews
    public void afterViews() {
        appNameView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (v.getHeight() == 0 || v.getWidth() == 0) return;
                appNamePlaceholderView.getLayoutParams().height = v.getHeight();
                appNamePlaceholderView.getLayoutParams().width = v.getWidth();
                appNameView.removeOnLayoutChangeListener(this);
                appNamePlaceholderView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        appNamePlaceholderView.removeOnLayoutChangeListener(this);
                        setAppNameViewLeftTop(appNamePlaceholderView.getLeft(), appNamePlaceholderView.getTop(), false);
                        appNameViewInitialPosition = true;
                    }
                });
            }
        });

        if(BuildConfig.BUILD_TYPE.equals("debug")) {
            loginEmailView.setText("huang.to@husky.neu.edu");
            loginPasswordView.setText("password");
        }
    }

    private void setAppNameViewLeftTop(int left, int top, boolean animate) {
        if (animate) {
            appNameView.animate().y(top).x(left);
        } else {
            appNameView.setX(left);
            appNameView.setY(top);
        }
    }

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
            Timber.d("" + huskyEmailPattern.matcher(email).matches());
            if (!huskyEmailPattern.matcher(email).matches()) {
                RegistrationCodeDialog.getRegistrationCode(getContext(), registrationCode -> {
                    EventBus.getDefault().post(new RequestRegisterEvent(email, password, registrationCode));
                });
            } else {
                EventBus.getDefault().post(new RequestRegisterEvent(email, password));
            }
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
                            // If the keyboard is shown, let's set non-visible layouts to GONE
                            // so they don't unnecessarily expand the containing FrameLayout.
                            int hiddenVisibility = keyboardIsShown ? View.GONE : View.INVISIBLE;
                            viewGroup.setVisibility(shouldShow ? View.VISIBLE : hiddenVisibility);
                            viewGroup.animate().setListener(null);
                        }
                    });
        } else {
            // See above comment.
            viewGroup.setVisibility(keyboardIsShown ? View.GONE : View.INVISIBLE);
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

    private void realignAppNameView() {
        if (!appNameViewInitialPosition) return;
        appNameView.post(() ->
                setAppNameViewLeftTop(appNamePlaceholderView.getLeft(), appNamePlaceholderView.getTop(), true));
    }

    @Override
    public void onKeyboardShown() {
        keyboardIsShown = true;
        taglineView.animate().alpha(0).setDuration(50);
        for (View view : secondaryFormViews) {
            view.setVisibility(View.GONE);
        }
        if (!getCurrentVisibleLayoutGroupViewType().equals(LayoutGroupViewType.REGISTER)) {
            // If the login form is shown while the keyboard is up,
            // let's set the register form to GONE so it doesn't take
            // up any space and the rest of the layout can size appropriately.
            registerLayoutGroup.setVisibility(View.GONE);
        }
        realignAppNameView();
    }

    @Override
    public void onKeyboardHidden() {
        keyboardIsShown = false;
        taglineView.animate().alpha(1).setDuration(50);
        for (View view : secondaryFormViews) {
            view.setVisibility(View.VISIBLE);
        }
        if (getCurrentVisibleLayoutGroupViewType().equals(LayoutGroupViewType.REGISTER)) {
            registerLayoutGroup.setVisibility(View.VISIBLE);
        } else {
            registerLayoutGroup.setVisibility(View.INVISIBLE);
        }
        realignAppNameView();
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
        public String registrationCode = null;

        public RequestRegisterEvent(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public RequestRegisterEvent(String username, String password, String registrationCode) {
            this.username = username;
            this.password = password;
            this.registrationCode = registrationCode;
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

        private final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);

        public boolean validate(String email, String password) {
            if (!emailPattern.matcher(email).matches()) {
                showToast(R.string.welcome_error_email_invalid);
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

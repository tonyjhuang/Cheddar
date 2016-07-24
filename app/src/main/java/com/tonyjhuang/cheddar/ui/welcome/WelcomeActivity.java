package com.tonyjhuang.cheddar.ui.welcome;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flyco.pageindicator.anim.select.ZoomInEnter;
import com.flyco.pageindicator.indicator.FlycoPageIndicaor;
import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.customviews.ButtonPagerLayout;
import com.tonyjhuang.cheddar.ui.customviews.ParallaxorViewPager;
import com.tonyjhuang.cheddar.ui.customviews.ParalloidImageView;
import com.tonyjhuang.cheddar.ui.dialog.LoadingDialog;
import com.tonyjhuang.cheddar.ui.list.ChatRoomListActivity_;
import com.tonyjhuang.cheddar.ui.welcome.verify_email.VerifyEmailActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.activity_welcome)
public class WelcomeActivity extends CheddarActivity implements WelcomeView, KeyboardObserver.KeyboardListener {

    @ViewById(R.id.container)
    RelativeLayout container;

    @ViewById(R.id.pager_layout)
    ButtonPagerLayout pagerLayout;

    @ViewById(R.id.view_pager)
    ParallaxorViewPager viewPager;

    @ViewById(R.id.background)
    ParalloidImageView backgroundView;

    @ViewById(R.id.pager_indicator)
    FlycoPageIndicaor pagerIndicatorView;

    @ViewById(R.id.husky)
    ImageView huskyView;

    @ViewById(R.id.version)
    TextView debugVersionView;

    @Bean(WelcomePresenterImpl.class)
    WelcomePresenter presenter;

    @Pref
    CheddarPrefs_ prefs;

    private WelcomePagerAdapter onboardAdapter;
    private LoadingDialog loadingDialog;

    @AfterInject
    void afterInject() {
        presenter.setView(this);
    }

    @AfterViews
    void afterViews() {
        debugVersionView.setText(getString(R.string.debug_label, getVersionName()));

        ViewPager.OnPageChangeListener pageListener = new ViewPager.SimpleOnPageChangeListener() {
            int savedHuskyTop = -1;

            @Override
            public void onPageSelected(int position) {
                int end = onboardAdapter.getCount() - 1;

                if (position == end) {
                    if (savedHuskyTop == -1) {
                        savedHuskyTop = huskyView.getTop();
                    }
                    pagerIndicatorView.animate().alpha(0);
                    huskyView.animate().y(savedHuskyTop + huskyView.getHeight()).setDuration(75);
                } else {
                    pagerIndicatorView.animate().alpha(1);
                    if (savedHuskyTop != -1) {
                        huskyView.animate().y(savedHuskyTop).setDuration(75);
                    }
                }
            }
        };

        boolean shouldShowOnboard = false;//!prefs.onboardShown().getOr(false);
        onboardAdapter = new WelcomePagerAdapter(getSupportFragmentManager(), shouldShowOnboard);
        viewPager.setAdapter(onboardAdapter);
        if (shouldShowOnboard) {
            pagerIndicatorView.setSelectAnimClass(ZoomInEnter.class).setViewPager(viewPager);
            viewPager.setOffscreenPageLimit(1);
            viewPager.addParalloid(backgroundView);
            viewPager.addOnPageChangeListener(pageListener);
        } else {
            pagerIndicatorView.setVisibility(View.GONE);
            huskyView.setVisibility(View.GONE);
        }
        pagerLayout.refresh();

        // Pretty ugly hack but we need to listen for layout resize events
        // to hide the husky when the keyboard is shown.
        KeyboardObserver keyboardObserver = new KeyboardObserver();
        keyboardObserver.addListener(this);
        keyboardObserver.setObservableLayout(container);
        container.getViewTreeObserver().addOnGlobalLayoutListener(keyboardObserver);
    }

    @Override
    public void showJoinChatLoadingDialog() {
        loadingDialog = LoadingDialog.show(this, R.string.chat_join_chat);
    }

    @Override
    public void showJoinChatFailed() {
        dismissLoadingDialog();
        showToast(R.string.onboard_error_join_chat);
    }

    @Override
    public void navigateToChatView(String aliasId) {
        dismissLoadingDialog();
        Intent chatIntent = ChatActivity_.intent(this).aliasId(aliasId).get();
        TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(chatIntent)
                .startActivities();
    }

    @Override
    public void showRegisterUserLoadingDialog() {
        loadingDialog = LoadingDialog.show(this, R.string.welcome_register_loading);
    }

    @Override
    public void showRegisterUserFailed() {
        showRegisterUserFailed(getString(R.string.welcome_register_failed));
    }

    @Override
    public void showRegisterUserFailed(String message) {
        if (loadingDialog != null) loadingDialog.dismiss();
        showToast(message);
    }

    @Override
    public void navigateToVerifyEmailView(String userId) {
        dismissLoadingDialog();
        VerifyEmailActivity_.intent(this).start();
    }

    @Override
    public void showLoginUserLoadingDialog() {
        loadingDialog = LoadingDialog.show(this, R.string.welcome_login_loading);
    }

    @Override
    public void showLoginUserFailed() {
        if (loadingDialog != null) loadingDialog.dismiss();
        showToast(R.string.welcome_error_credentials);
    }

    @Override
    public void navigateToListView() {
        ChatRoomListActivity_.intent(this).start();
    }

    @Override
    public void showNetworkConnectionError() {
        showToast(R.string.welcome_error_network);
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null) loadingDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = onboardAdapter.getItem(viewPager.getCurrentItem());
        if (currentFragment instanceof BackButtonHandler) {
            if (!((BackButtonHandler) currentFragment).handleBackPress()) {
                super.onBackPressed();
            }
        }
    }

    private void notifyCurrentFragmentKeyboardShown(boolean shown) {
        Fragment currentFragment = onboardAdapter.getItem(viewPager.getCurrentItem());
        if (currentFragment instanceof KeyboardObserver.KeyboardListener) {
            if (shown) {
                ((KeyboardObserver.KeyboardListener) currentFragment).onKeyboardShown();
            } else {
                ((KeyboardObserver.KeyboardListener) currentFragment).onKeyboardHidden();
            }
        }
    }

    @Override
    public void onKeyboardShown() {
        notifyCurrentFragmentKeyboardShown(true);
        debugVersionView.setVisibility(View.GONE);
    }

    @Override
    public void onKeyboardHidden() {
        notifyCurrentFragmentKeyboardShown(false);
        debugVersionView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @EFragment(R.layout.fragment_onboard_cheddar)
    public static class OnboardCheddarFragment extends Fragment {
    }

    @EFragment(R.layout.fragment_onboard_match)
    public static class OnboardMatchFragment extends Fragment {
    }

    @EFragment(R.layout.fragment_onboard_group)
    public static class OnboardGroupFragment extends Fragment {
    }
}

package com.tonyjhuang.cheddar.ui.welcome;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.flyco.pageindicator.anim.select.ZoomInEnter;
import com.flyco.pageindicator.indicator.FlycoPageIndicaor;
import com.tonyjhuang.cheddar.BuildConfig;
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

@EActivity(R.layout.activity_main)
public class WelcomeActivity extends CheddarActivity implements WelcomeView {

    @ViewById(R.id.pager_layout)
    ButtonPagerLayout pagerLayout;

    @ViewById(R.id.view_pager)
    ParallaxorViewPager viewPager;

    @ViewById
    ParalloidImageView background;

    @ViewById(R.id.pager_indicator)
    FlycoPageIndicaor indicator;

    @ViewById(R.id.debug_label)
    View debugLabel;

    @ViewById(R.id.husky)
    View huskyView;

    @ViewById(R.id.version)
    TextView versionView;

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
        if (BuildConfig.DEBUG) {
            debugLabel.setVisibility(View.VISIBLE);
            versionView.setText(getVersionName());
        }

        ViewPager.OnPageChangeListener pageListener = new ViewPager.SimpleOnPageChangeListener() {

            // last page selected.
            int prevPosition = 0;

            @Override
            public void onPageSelected(int position) {
                int end = onboardAdapter.getCount() - 1;

                if (position == end) {
                    indicator.animate().alpha(0);
                    huskyView.animate().setDuration(100).yBy(huskyView.getHeight());
                } else {
                    indicator.animate().alpha(1);
                }

                if (position == end - 1 && prevPosition == end) {
                    huskyView.animate().setDuration(100).yBy(-huskyView.getHeight());
                }

                prevPosition = position;
            }
        };

        boolean shouldShowOnboard = !prefs.onboardShown().getOr(false);
        onboardAdapter = new WelcomePagerAdapter(getSupportFragmentManager(), shouldShowOnboard);
        viewPager.setAdapter(onboardAdapter);
        viewPager.setOffscreenPageLimit(1);
        indicator.setSelectAnimClass(ZoomInEnter.class).setViewPager(viewPager);
        viewPager.addParalloid(background);
        viewPager.addOnPageChangeListener(pageListener);
        pagerLayout.refresh();
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
        if (loadingDialog != null) loadingDialog.dismiss();
        showToast(R.string.welcome_register_failed);
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
        showToast(R.string.welcome_login_failed);
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

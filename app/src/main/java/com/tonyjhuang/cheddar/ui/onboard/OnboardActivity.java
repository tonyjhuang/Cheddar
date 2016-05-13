package com.tonyjhuang.cheddar.ui.onboard;

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
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.customviews.ParallaxorViewPager;
import com.tonyjhuang.cheddar.ui.customviews.ParalloidImageView;
import com.tonyjhuang.cheddar.ui.dialog.LoadingDialog;
import com.tonyjhuang.cheddar.ui.list.ChatRoomListActivity_;
import com.tonyjhuang.cheddar.ui.login.VerifyEmailActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

@EActivity(R.layout.activity_main)
public class OnboardActivity extends CheddarActivity implements OnboardView {

    @ViewById(R.id.view_pager)
    ParallaxorViewPager viewPager;

    @ViewById
    ParalloidImageView background;

    @ViewById(R.id.pager_indicator)
    FlycoPageIndicaor indicator;

    @ViewById(R.id.pager_left)
    View pagerLeft;

    @ViewById(R.id.pager_right)
    View pagerRight;

    @ViewById(R.id.debug_label)
    View debugLabel;

    @ViewById(R.id.husky)
    View husky;

    @ViewById
    TextView version;

    @Bean(OnboardPresenterImpl.class)
    OnboardPresenter presenter;

    /**
     * ViewPager adapter.
     */
    private OnboardPagerAdapter onboardAdapter;

    private LoadingDialog loadingDialog;

    @AfterInject
    void afterInject() {
        presenter.setView(this);
    }

    @AfterViews
    void afterViews() {
        if (BuildConfig.DEBUG) {
            debugLabel.setVisibility(View.VISIBLE);
            version.setText(getVersionName());
        }

        onboardAdapter = new OnboardPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(onboardAdapter);
        viewPager.setOffscreenPageLimit(1);
        indicator.setSelectAnimClass(ZoomInEnter.class).setViewPager(viewPager);
        viewPager.addParalloid(background);

        pagerLeft.setAlpha(0);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            // last page selected.
            int prevPosition = 0;

            @Override
            public void onPageSelected(int position) {
                int end = onboardAdapter.getCount() - 1;
                if (position == 0 || position == end) {
                    pagerLeft.animate().alpha(0);
                } else {
                    pagerLeft.animate().alpha(1);
                }

                if (position == end) {
                    indicator.animate().alpha(0);
                    pagerRight.animate().alpha(0);
                    husky.animate().setDuration(100).yBy(husky.getHeight());
                } else {
                    indicator.animate().alpha(1);
                    pagerRight.animate().alpha(1);
                }

                if (position == end - 1 && prevPosition == end) {
                    husky.animate().setDuration(100).yBy(-husky.getHeight());
                }

                prevPosition = position;
            }
        });
    }

    @Click(R.id.pager_left)
    public void onPagerLeftClick() {
        viewPager.setCurrentItem(Math.max(0, viewPager.getCurrentItem() - 1));
    }

    @Click(R.id.pager_right)
    public void onPagerRightClick() {
        viewPager.setCurrentItem(Math.min(onboardAdapter.getCount() - 1,
                viewPager.getCurrentItem() + 1));
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
        loadingDialog = LoadingDialog.show(this, R.string.signup_register_loading);
    }

    @Override
    public void showRegisterUserFailed() {
        if (loadingDialog != null) loadingDialog.dismiss();
        showToast(R.string.signup_register_failed);
    }

    @Override
    public void navigateToVerifyEmailView(String userId) {
        dismissLoadingDialog();
        VerifyEmailActivity_.intent(this).start();
    }

    @Override
    public void showLoginUserLoadingDialog() {
        loadingDialog = LoadingDialog.show(this, R.string.signup_login_loading);
    }

    @Override
    public void showLoginUserFailed() {
        if (loadingDialog != null) loadingDialog.dismiss();
        showToast(R.string.signup_login_failed);
    }

    @Override
    public void navigateToListView() {
        ChatRoomListActivity_.intent(this).start();
    }

    @Override
    public void showNetworkConnectionError() {
        showToast(R.string.signup_error_network);
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

    @EFragment(R.layout.fragment_alpha_warning)
    public static class AlphaWarningFragment extends Fragment {
        @Click(R.id.confirm)
        public void onConfirmClick() {
            EventBus.getDefault().post(new JoinChatEvent());
        }

        public static class JoinChatEvent {
        }
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

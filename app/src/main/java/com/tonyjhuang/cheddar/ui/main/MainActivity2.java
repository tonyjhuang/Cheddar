package com.tonyjhuang.cheddar.ui.main;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.flyco.pageindicator.anim.select.ZoomInEnter;
import com.flyco.pageindicator.indicator.FlycoPageIndicaor;
import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.CheddarPrefs_;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.CheddarMetricTracker;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.customviews.ParallaxorViewPager;
import com.tonyjhuang.cheddar.ui.customviews.ParalloidImageView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjhuang on 3/23/16.
 */
@EActivity(R.layout.activity_main_2)
public class MainActivity2 extends CheddarActivity {

    private static final String TAG = MainActivity2.class.getSimpleName();

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

    @ViewById(R.id.husky)
    View husky;

    @Bean
    CheddarApi cheddarApi;

    @Pref
    CheddarPrefs_ prefs;

    // ViewPager adapter.
    private MainPagerAdapter onboardAdapter;

    @AfterViews
    void updateViews() {
        onboardAdapter = new MainPagerAdapter(getSupportFragmentManager());
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
                int numItems = onboardAdapter.getCount();
                if (position == 0 || position == numItems) {
                    pagerLeft.animate().alpha(0);
                } else {
                    pagerLeft.animate().alpha(1);
                }

                if (position == numItems) {
                    indicator.animate().alpha(0);
                    pagerRight.animate().alpha(0);
                    husky.animate().setDuration(100).yBy(husky.getHeight());
                } else {
                    indicator.animate().alpha(1);
                    pagerRight.animate().alpha(1);
                }

                if (position == numItems - 1 && prevPosition == numItems) {
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

    public void onEvent(AlphaWarningFragment.JoinChatEvent event) {
        Log.d(TAG, "onEvent!");
        subscribe(cheddarApi.joinNextAvailableChatRoom(5), alias -> {
            CheddarMetricTracker.trackJoinChatRoom(alias.getChatRoomId());
            ChatActivity_.intent(this).aliasId(alias.getObjectId()).start();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
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

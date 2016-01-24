package com.tonyjhuang.chatly;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.SeekBarProgressChange;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DimensionPixelOffsetRes;
import org.androidannotations.annotations.res.StringArrayRes;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.activity_main)
public class MainActivity extends CheddarActivity {

    @ViewById(R.id.view_pager)
    ViewPagerCustomDuration viewPager;

    @ViewById(R.id.foreground_container)
    ViewGroup foregroundContainer;

    @ViewById(R.id.back_splash)
    View backSplash;

    @ViewById
    ImageView cheese;

    @ViewById(R.id.debug_seeker_position)
    TextView debugSeekerPosition;

    @ViewById(R.id.welcome_to)
    TextView welcomeTo;

    @ViewById
    ProgressBar loading;

    @ViewById(R.id.loading_text)
    TextView loadingText;

    @ViewById(R.id.black_out)
    View blackOut;

    @DimensionPixelOffsetRes(R.dimen.welcome_left_margin)
    int welcomeToInitialLeftMargin;

    @DimensionPixelOffsetRes(R.dimen.cheese_extra_horizontal_padding)
    int cheesePadding;

    @DimensionPixelOffsetRes(R.dimen.foreground_final_vertical_padding)
    int foregroundVerticalPadding;

    // Have we tried animating?
    private boolean dirty = false;

    private float initialForegroundY;

    private Handler animationHandler = new Handler();

    public static int getScreenHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    @AfterViews
    void updateViews() {
        viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    // Animate "WELCOME TO" TextView.
                    RelativeLayout.LayoutParams lp =
                            (RelativeLayout.LayoutParams) welcomeTo.getLayoutParams();
                    lp.setMargins((int) (welcomeToInitialLeftMargin - (positionOffsetPixels * .25)),
                            lp.topMargin, lp.rightMargin, lp.bottomMargin);
                    welcomeTo.setLayoutParams(lp);
                    welcomeTo.setAlpha((float) ((positionOffset - .66) * -3));

                    // Parallax cheese background horizontally.
                    RelativeLayout.LayoutParams lp2 =
                            (RelativeLayout.LayoutParams) cheese.getLayoutParams();
                    lp2.setMargins((int) (-positionOffset * cheesePadding),
                            lp2.topMargin, (int) ((1 - positionOffset) * cheesePadding * -1), lp2.bottomMargin);
                    cheese.setLayoutParams(lp2);
                }
            }
        });
        viewPager.setSwipeable(false);
        viewPager.setScrollDurationFactor(4.5);
        viewPager.setCurrentItem(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            if (CheddarApplication.DEBUG && dirty) {
                EventBus.getDefault().post(new RequestResetEvent());
                reset();
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        }
    }

    private void reset() {
        dirty = false;
        foregroundContainer.setY(initialForegroundY);
        loading.setAlpha(0f);
        loadingText.setAlpha(0f);
        loadingText.setText(getResources().getString(R.string.loading));
        blackOut.setAlpha(0f);
    }

    @SeekBarProgressChange(R.id.debug_seeker)
    public void onDebugSeekerPositionChanged(int position) {
        debugSeekerPosition.setText(position * 100 + "");
    }

    public void onEvent(GetStartedFragment.NextClickEvent event) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    public void onEvent(JoinChatFragment.MatchClickEvent event) {
        dirty = true;

        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateForeground();
            }
        }, 200);
    }

    // time: 200
    private void animateForeground() {
        initialForegroundY = foregroundContainer.getY();
        foregroundContainer.animate()
                .y(getScreenHeight(this) - foregroundVerticalPadding)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(500);

        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateLoading();
            }
        }, 550);
    }

    // time: 750
    private void animateLoading() {
        loading.animate().alpha(1).setDuration(1000);

        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateLoadingText();
            }
        }, 750);
    }

    // time: 1500
    private void animateLoadingText() {
        loadingText.animate().alpha(1).setDuration(750);

        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // time: 3500
                loadingText.animate().alpha(0).setDuration(500);

                animationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // time: 4000
                        loadingText.setText(getResources().getString(R.string.loaded));
                        loadingText.animate().alpha(1).setDuration(750);

                        animationHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animateBlackOut();
                            }
                        }, 1500);
                    }
                }, 500);
            }
        }, 2000);

    }

    // time: 5500
    private void animateBlackOut() {
        blackOut.animate().alpha(1).setDuration(1500);
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startCountdownActivity();
            }
        }, 1500);
    }

    private void startCountdownActivity() {
        startActivity(new Intent(this, CountdownActivity_.class));
        this.overridePendingTransition(0, 0);
    }

    public static class RequestResetEvent {
    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MainActivity_.GetStartedFragment_();
                default:
                    return new MainActivity_.JoinChatFragment_();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @EFragment(R.layout.fragment_get_started)
    public static class GetStartedFragment extends Fragment {

        @ViewById
        TextView description;

        @AfterViews
        void boldDescriptionText() {
            boldSubstring(description, "anonymous");
        }

        private void boldSubstring(TextView textView, String substring) {
            String text = textView.getText().toString();
            int index = text.indexOf(substring);
            final SpannableStringBuilder builder = new SpannableStringBuilder(text);
            builder.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    index, index + substring.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(builder);
        }

        @Click(R.id.lets_go)
        void onLetsGoButtonClick() {
            EventBus.getDefault().post(new NextClickEvent());
        }

        public static class NextClickEvent {
        }
    }


    @EFragment(R.layout.fragment_join_chat)
    public static class JoinChatFragment extends Fragment {

        @ViewById(R.id.cardview)
        CardView cardView;

        @ViewById(R.id.hall_spinner)
        Spinner residenceHallPicker;

        @ViewById(R.id.single_item)
        FrameLayout singleItem;

        @ViewById(R.id.single_image)
        ImageView singleItemImage;

        @ViewById(R.id.single_image_selected)
        ImageView singleItemSelectedImage;

        @ViewById(R.id.group_image)
        ImageView groupItemImage;

        @ViewById(R.id.group_image_selected)
        ImageView groupItemSelectedImage;

        @ViewById(R.id.group_item)
        FrameLayout groupItem;

        @StringArrayRes(R.array.residence_halls)
        String[] residenceHalls;

        @ColorRes(R.color.matchItemSelected)
        int matchItemSelectedColor;

        @ColorRes(R.color.matchItemUnselected)
        int matchItemUnselectedColor;

        // Which option do we match for, single or group? Default to Single.
        private boolean isSingle = true;

        private float initialCardViewY;

        @Override
        public void onStart() {
            super.onStart();
            EventBus.getDefault().register(this);
        }

        @Override
        public void onStop() {
            EventBus.getDefault().unregister(this);
            super.onStop();
        }

        @AfterViews
        void updateViews() {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.residence_halls, R.layout.stub_residence_hall_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            residenceHallPicker.setAdapter(adapter);
            setIsSingle(true);
        }

        private void setIsSingle(boolean isSingle) {
            this.isSingle = isSingle;
            singleItem.setBackgroundColor(isSingle ? matchItemSelectedColor : matchItemUnselectedColor);
            singleItemImage.setAlpha(isSingle ? 0f : .6f);
            singleItemSelectedImage.setAlpha(isSingle ? 1f : 0f);
            groupItem.setBackgroundColor(isSingle ? matchItemUnselectedColor : matchItemSelectedColor);
            groupItemImage.setAlpha(isSingle ? .6f : 0f);
            groupItemSelectedImage.setAlpha(isSingle ? 0f : 1f);
        }

        @ItemSelect(R.id.hall_spinner)
        void onResidenceHallItemClicked(boolean selected, int position) {
            setIsSingle(true);
        }

        @Click(R.id.match)
        void onMatchButtonClick() {
            if (isSingle) {
                String hall = String.valueOf(residenceHallPicker.getSelectedItem());
                EventBus.getDefault().post(new MatchClickEvent(true,
                        hall.equals(residenceHalls[0]) ? null : hall));
            } else {
                EventBus.getDefault().post(new MatchClickEvent(false, null));
            }
            animateCardView();
        }

        private void animateCardView() {
            initialCardViewY = cardView.getY();
            cardView.animate()
                    .y(getScreenHeight(getContext()))
                    .setInterpolator(new AnticipateInterpolator(1.5f))
                    .setDuration(500);
        }

        @Click(R.id.single_click_target)
        void onSingleItemClick() {
            setIsSingle(true);
        }

        @Click(R.id.group_click_target)
        void onGroupItemClick() {
            setIsSingle(false);
        }

        public void onEvent(RequestResetEvent event) {
            // reset here.
            cardView.setY(initialCardViewY);
        }

        public static class MatchClickEvent {
            // Can be null to denote the user did not pick a dorm. Is null if isSingle is false.
            public final String residenceHall;
            public final boolean isSingle;

            public MatchClickEvent(boolean isSingle, String residenceHall) {
                this.isSingle = isSingle;
                this.residenceHall = residenceHall;
            }
        }
    }
}

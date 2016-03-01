package com.tonyjhuang.cheddar.ui.main;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonyjhuang.cheddar.CheddarActivity;
import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.CheddarApi;
import com.tonyjhuang.cheddar.api.models.Alias;
import com.tonyjhuang.cheddar.ui.chat.ChatActivity_;
import com.tonyjhuang.cheddar.ui.customviews.ParallaxorViewPager;
import com.tonyjhuang.cheddar.ui.customviews.ParalloidImageView;
import com.tonyjhuang.cheddar.ui.utils.DisplayHelper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;
import org.androidannotations.annotations.res.DimensionPixelOffsetRes;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;

@EActivity(R.layout.activity_main)
public class MainActivity extends CheddarActivity {

    @ViewById(R.id.view_pager)
    ParallaxorViewPager viewPager;

    @ViewById(R.id.foreground_container)
    ViewGroup foregroundContainer;

    @ViewById(R.id.back_splash)
    View backSplash;

    @ViewById
    ParalloidImageView cheese;

    @ViewById(R.id.welcome_to_container)
    WelcomeToParalloidViewLayout welcomeToContainer;

    @ViewById
    ImageView loading;

    @ViewById(R.id.loading_text)
    TextView loadingText;

    @ViewById(R.id.black_out)
    View blackOut;

    @DimensionPixelOffsetRes(R.dimen.foreground_final_vertical_padding)
    int foregroundVerticalPadding;

    @AnimationRes(R.anim.spin)
    Animation loadingAnimation;

    @Bean
    CheddarApi cheddarApi;

    private Handler animationHandler = new Handler();

    @AfterInject
    void logout() {
        cheddarApi.logout().publish();
    }

    @AfterViews
    void updateViews() {
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        viewPager.addParalloid(cheese);
        viewPager.addParalloid((p) -> {
            // We only want welcomeToContainer to parallax on the first item.
            float parallaxFactor = Math.min(p * viewPager.getAdapter().getCount(), 1);
            welcomeToContainer.parallaxBy(parallaxFactor);
        });
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
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //vstartChatActivity("csRVomohCP");
    }

    private void startChatActivity(String aliasId) {
        //animateHideLoading();
        animationHandler.postDelayed(() ->
                ChatActivity_.intent(this).aliasId(aliasId).start(), 500);
    }

    /*
     *
     *     EVENT LISTENER
     *
     */

    public void onEvent(GetStartedFragment.NextClickEvent event) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    public void onEvent(SignUpFragment.SubmitClickEvent event) {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    public void onEvent(JoinChatFragment.MatchClickEvent event) {
        Observable<Alias> getAliasAfterAnimation = animateJoinChat()
                .zipWith(cheddarApi.joinNextAvailableChatRoom(event.maxOccupancy),
                        (time, alias) -> alias);
        subscribe(getAliasAfterAnimation,
                alias -> startChatActivity(alias.getObjectId()),
                throwable -> Log.e("CHAT", throwable.toString()));
    }

    /*
     *
     *    ANIMATIONS
     *
     */

    private Observable<Long> animateJoinChat() {
        animationHandler.postDelayed(this::animateForeground, 200);
        animationHandler.postDelayed(this::animateLoading, 750);
        animationHandler.postDelayed(this::animateJoinChatText, 750);
        return Observable.timer(1250, TimeUnit.MILLISECONDS);
    }

    private void animateForeground() {
        foregroundContainer.animate()
                .y(DisplayHelper.getScreenHeight(this) - foregroundVerticalPadding)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(500);
    }

    private void animateLoading() {
        loading.animate().alpha(1).setDuration(500);
        loading.startAnimation(loadingAnimation);
    }

    private void animateJoinChatText() {
        loadingText.animate().alpha(1).setDuration(500);
    }

    private void animateHideLoading() {
        loadingText.animate().alpha(0).setDuration(500);
    }
}

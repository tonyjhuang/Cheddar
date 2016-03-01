package com.tonyjhuang.cheddar.ui.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.ui.customviews.ParalloidViewLayout;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.res.DimensionPixelOffsetRes;

/**
 * Created by tonyjhuang on 2/9/16.
 * Container to parallax the WELCOME TO textview on MainActivity.
 */
@EViewGroup
public class WelcomeToParalloidViewLayout extends ParalloidViewLayout {

    @DimensionPixelOffsetRes(R.dimen.welcome_left_margin)
    int leftMargin;

    private View parallaxView;

    public WelcomeToParalloidViewLayout(Context context) {
        super(context);
    }

    public WelcomeToParalloidViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WelcomeToParalloidViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        parallaxView = getParallaxView();
    }

    @Override
    public void parallaxBy(float parallaxFactor) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getLayoutParams();
        lp.setMargins((int) (leftMargin * parallaxFactor * .25) * -1,
                lp.topMargin, lp.rightMargin, lp.bottomMargin);
        setLayoutParams(lp);
        parallaxView.setAlpha((float) ((parallaxFactor - .66) * -3));
    }
}

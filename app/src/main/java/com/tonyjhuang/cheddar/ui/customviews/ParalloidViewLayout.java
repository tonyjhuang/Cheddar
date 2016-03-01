package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by tonyjhuang on 2/9/16.
 * FrameLayout that contains a single view that knows how to parallax that view.
 */
public abstract class ParalloidViewLayout extends FrameLayout implements ParallaxorViewPager.Paralloid {
    public ParalloidViewLayout(Context context) {
        this(context, null);
    }

    public ParalloidViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParalloidViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected View getParallaxView() {
        return getChildAt(0);
    }
}

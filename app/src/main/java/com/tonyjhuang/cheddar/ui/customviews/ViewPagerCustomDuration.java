package com.tonyjhuang.cheddar.ui.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.tonyjhuang.cheddar.R;

import java.lang.reflect.Field;

public class ViewPagerCustomDuration extends ViewPager {

    private ScrollerCustomDuration mScroller = null;
    private boolean swipeable = true;

    public ViewPagerCustomDuration(Context context) {
        this(context, null);
        postInitViewPager();
    }

    public ViewPagerCustomDuration(Context context, AttributeSet attrs) {
        super(context, attrs);

        postInitViewPager();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerCustomDuration);
            swipeable = a.getBoolean(R.styleable.ViewPagerCustomDuration_swipeable, true);
            setScrollDurationFactor(a.getFloat(R.styleable.ViewPagerCustomDuration_duration, 1f));
            a.recycle();
        }
    }



    public void setSwipeable(boolean swipeable) {
        this.swipeable = swipeable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (swipeable) {
            return super.onInterceptTouchEvent(event);
        } else {
            // Never allow swiping to switch between pages
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipeable) {
            return super.onTouchEvent(event);
        } else {
            // Never allow swiping to switch between pages
            return false;
        }
    }

    /**
     * Override the Scroller instance with our own class so we can change the
     * duration
     */
    private void postInitViewPager() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = viewpager.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            mScroller = new ScrollerCustomDuration(getContext(),
                    (Interpolator) interpolator.get(null));
            scroller.set(this, mScroller);
        } catch (Exception e) {
        }
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollDurationFactor(double scrollFactor) {
        mScroller.setScrollDurationFactor(scrollFactor);
    }

    private static class ScrollerCustomDuration extends Scroller {

        private double mScrollFactor = 1;

        public ScrollerCustomDuration(Context context) {
            super(context);
        }

        public ScrollerCustomDuration(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @SuppressLint("NewApi")
        public ScrollerCustomDuration(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        /**
         * Set the factor by which the duration will change
         */
        public void setScrollDurationFactor(double scrollFactor) {
            mScrollFactor = scrollFactor;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, (int) (duration * mScrollFactor));
        }

    }

}
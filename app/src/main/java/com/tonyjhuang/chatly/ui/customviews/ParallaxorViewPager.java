package com.tonyjhuang.chatly.ui.customviews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjhuang on 2/5/16.
 */
public class ParallaxorViewPager extends ViewPagerCustomDuration {

    private List<Paralloid> paralloids = new ArrayList<>();

    public ParallaxorViewPager(Context context) {
        super(context);
        init();
    }

    public ParallaxorViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if(isInEditMode()) return;

        super.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int totalItems = getAdapter().getCount();
                // Need to subtract 1 here because you can't scroll past the n-1th index.
                // for example: if you have two fragments, position = 0, positionOffset = [0, 1)
                // so baseParallax (without - 1) would be 0, max parallaxFactor would be
                // 0 + 1 / 2 = .5 whereas we want it to go to 1 to denote you can't swipe
                // anymore.
                float baseParallax = (float) position / (totalItems - 1);
                float parallaxFactor = baseParallax + (positionOffset / (totalItems - 1));
                for (Paralloid paralloid : paralloids) {
                    paralloid.parallaxBy(parallaxFactor);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    // Takes a View that implements Paralloid and calls parallaxBy with a number [0, 1]
    public void addParalloid(Paralloid paralloid) {
        paralloids.add(paralloid);
    }

    public interface Paralloid {
        void parallaxBy(float parallaxFactor);
    }
}

package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import timber.log.Timber;

/**
 * Wraps a ViewPager, adds buttons that page left and right.
 * Hides the left button on the leftmost and rightmost fragment view.
 * Hides the right button on rightmost fragment view.
 */
@EViewGroup(R.layout.view_button_pager_layout)
public class ButtonPagerLayout extends RelativeLayout {

    @ViewById(R.id.pager_left)
    View pagerLeftView;
    @ViewById(R.id.pager_right)
    View pagerRightView;

    private ViewPager pager;

    private final ViewPager.OnPageChangeListener pageListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (pager.getAdapter() == null) return;
            int end = pager.getAdapter().getCount() - 1;
            boolean isLeftMost = position == 0;
            boolean isRightMost = position == end;
            pagerLeftView.animate().alpha(isLeftMost || isRightMost ? 0 : 1);
            pagerRightView.animate().alpha(isRightMost ? 0 : 1);
        }
    };

    public ButtonPagerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @AfterViews
    public void afterViews() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ViewPager) {
                pager = (ViewPager) child;
                pager.addOnPageChangeListener(pageListener);
                return;
            }
        }
        Timber.e("Missing view pager");
        pagerLeftView.setVisibility(GONE);
        pagerRightView.setVisibility(GONE);
    }

    public void refresh() {
        pageListener.onPageSelected(pager.getCurrentItem());
    }

    @Click(R.id.pager_left)
    public void onPagerLeftClick() {
        pager.setCurrentItem(Math.max(0, pager.getCurrentItem() - 1));
    }

    @Click(R.id.pager_right)
    public void onPagerRightClick() {
        pager.setCurrentItem(Math.min(pager.getCurrentItem() + 1, pager.getAdapter().getCount() - 1));
    }
}

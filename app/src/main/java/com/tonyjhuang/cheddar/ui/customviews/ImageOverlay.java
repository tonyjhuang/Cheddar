package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by tonyjhuang on 2/27/16.
 */
public class ImageOverlay extends FrameLayout {
    public ImageOverlay(Context context) {
        super(context);
    }

    public ImageOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void show(int childIndex) {
        show(childIndex, true);
    }

    public void show(int childIndex, boolean animate) {
        display(true, childIndex, animate);
    }

    public void hide(int childIndex) {
        hide(childIndex, true);
    }

    public void hide(int childIndex, boolean animate) {
        display(false, childIndex, animate);
    }

    private void display(boolean show, int childIndex, boolean animate) {
        if(0 > childIndex || getChildCount() <= childIndex) {
            return;
        }
        getChildAt(childIndex).animate().alpha(show ? 1 : 0).setDuration(animate ? 150 : 0);
    }
}

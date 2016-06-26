package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tonyjhuang.cheddar.R;

/**
 * Created by tonyjhuang on 2/6/16.
 */
public class ParalloidImageView extends ImageView implements ParallaxorViewPager.Paralloid {

    private float parallaxAmount;

    private float initialLeftMargin = 0;
    private float initialRightMargin = 0;

    public ParalloidImageView(Context context) {
        this(context, null);
    }

    public ParalloidImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ParalloidImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ParalloidImageView, defStyleAttr, 0);
            parallaxAmount = a.getDimension(R.styleable.ParalloidImageView_parallaxAmount, 0) * -1;
            a.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            initialLeftMargin = mlp.leftMargin;
            initialRightMargin = mlp.rightMargin;
        }
        parallaxBy(0);
    }

    @Override
    public void parallaxBy(float parallaxFactor) {
        // Parallax cheese background horizontally.
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            setLeftMargin(mlp, initialLeftMargin + parallaxFactor * parallaxAmount);
            setTopMargin(mlp, mlp.topMargin);
            setRightMargin(mlp, initialRightMargin + (1 - parallaxFactor) * parallaxAmount);
            setBottomMargin(mlp, mlp.bottomMargin);
        }
        setLayoutParams(lp);
    }

    private void setLeftMargin(ViewGroup.MarginLayoutParams lp, float margin) {
        lp.setMargins((int) margin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
    }

    private void setTopMargin(ViewGroup.MarginLayoutParams lp, float margin) {
        lp.setMargins(lp.leftMargin, (int) margin, lp.rightMargin, lp.bottomMargin);
    }

    private void setRightMargin(ViewGroup.MarginLayoutParams lp, float margin) {
        lp.setMargins(lp.leftMargin, lp.topMargin, (int) margin, lp.bottomMargin);
    }

    private void setBottomMargin(ViewGroup.MarginLayoutParams lp, float margin) {
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, (int) margin);
    }
}

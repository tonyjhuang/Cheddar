package com.tonyjhuang.cheddar.ui.chat;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;

import org.androidannotations.annotations.EView;

/**
 * YOU MUST SET THE STYLE TO R.style.AliasDisplay IN XML
 * SINCE YOU CANNOT UPDATE STYLES PROGRAMMATICALLY (for some reason).
 */
@EView
public class AliasDisplayView extends TextView {

    public AliasDisplayView(Context context) {
        this(context, null);
    }

    public AliasDisplayView(Context context, AttributeSet attrs) {
        this(context, attrs, R.style.AliasDisplay);
    }

    public AliasDisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((GradientDrawable) getBackground()).setCornerRadius(getMeasuredHeight() / 2);
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    public void setAliasName(String aliasName) {
        String display = "";
        for (String namePart : aliasName.split(" ")) {
            display += namePart.substring(0, 1).toUpperCase();
        }
        setText(display);
    }

    public void setColor(int color) {
        ((GradientDrawable) getBackground()).setColor(color);
    }
}

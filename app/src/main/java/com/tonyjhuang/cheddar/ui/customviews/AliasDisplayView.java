package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.value.Alias;

import org.androidannotations.annotations.EView;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.IntArrayRes;

/**
 * YOU MUST SET THE STYLE TO R.style.AliasDisplay IN XML
 * SINCE YOU CANNOT UPDATE STYLES PROGRAMMATICALLY (for some reason).
 */
@EView
public class AliasDisplayView extends TextView {
    private static Typeface typeface;

    @ColorRes(R.color.chat_author_text)
    int textColor;

    @ColorRes(R.color.chat_author_background_outgoing)
    int outgoingBackgroundColor;

    @IntArrayRes(R.array.chat_author_color)
    int[] incomingBackgroundColors;

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

    private void init() {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(getContext().getAssets(), "Effra-Medium.ttf");

        }
    }

    public void setAlias(Alias alias, boolean isCurrentUser) {
        init();

        setAliasName(alias.name());
        setTextColor(textColor);
        setTypeface(typeface);

        if (isCurrentUser) {
            setBgColor(outgoingBackgroundColor);
        } else {
            int colorId = alias.colorId();
            if (colorId < 0 || colorId >= incomingBackgroundColors.length) colorId = 0;
            setBgColor(incomingBackgroundColors[colorId]);
        }
    }

    public void setAliasName(String aliasName) {
        String display = "";
        for (String namePart : aliasName.split(" ")) {
            display += namePart.substring(0, 1).toUpperCase();
        }
        setText(display);
    }

    public void setBgColor(int color) {
        ((GradientDrawable) getBackground()).setColor(color);
    }
}

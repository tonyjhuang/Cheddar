package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tonyjhuang.cheddar.R;
import com.tonyjhuang.cheddar.api.models.value.Alias;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.DimensionRes;
import org.androidannotations.annotations.res.IntArrayRes;

/**
 * Displays a single Alias in a bubble using its initials.
 */
@EViewGroup(R.layout.view_alias_display)
public class AliasDisplayView extends FrameLayout {
    @ColorRes(R.color.chat_author_text)
    int textColor;

    @ColorRes(R.color.chat_author_background_outgoing)
    int outgoingBackgroundColor;

    @IntArrayRes(R.array.chat_author_color)
    int[] incomingBackgroundColors;

    @DimensionRes(R.dimen.chat_bubble_author_text_size)
    float defaultAuthorTextSize;
    @ViewById(R.id.background)
    View background;
    @ViewById(R.id.unread_indicator)
    View unreadIndicator;
    @ViewById(R.id.author_text)
    TextView authorText;
    /**
     * Size of our authorText set in AttributeSet.
     */
    private float authorTextSize;

    public AliasDisplayView(Context context) {
        this(context, null);
    }

    public AliasDisplayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AliasDisplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AliasDisplayView);
            authorTextSize = a.getDimension(R.styleable.AliasDisplayView_textSize, -1);
            a.recycle();
        }

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                resize();
            }
        });
    }

    /**
     * Resize the view according to its current dimensions. Should only be called publicly
     * if you've manually changed the view dimens and you need to force a resize.
     */
    public void resize() {
        int measuredHeight = getMeasuredHeight();
        ((GradientDrawable) background.getBackground()).setCornerRadius(measuredHeight / 2);

        // Style unread message indicator.
        // Lots of magic numbers ^_^
        int unreadIndicatorDimen = measuredHeight / 4;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(unreadIndicatorDimen, unreadIndicatorDimen);
        lp.leftMargin = measuredHeight / 14;
        unreadIndicator.setLayoutParams(lp);
        GradientDrawable backgroundGradient = (GradientDrawable) unreadIndicator.getBackground();
        backgroundGradient.setStroke(unreadIndicatorDimen / 7, getResources().getColor(R.color.ui_light_gray));
    }

    @AfterViews
    public void afterViews() {
        if (authorTextSize == -1 || authorTextSize == 0) {
            authorTextSize = defaultAuthorTextSize;
        }
        setTextSize(authorTextSize);
    }

    public void setAlias(Alias alias, boolean isCurrentUser) {
        setAliasName(alias.name());
        authorText.setTextColor(textColor);
        authorText.setTextColor(getResources().getColor(R.color.text_primary_light));

        if (isCurrentUser) {
            setBgColor(outgoingBackgroundColor);
        } else {
            int colorId = alias.colorId();
            if (colorId < 0 || colorId >= incomingBackgroundColors.length) colorId = 0;
            setBgColor(incomingBackgroundColors[colorId]);
        }
    }

    private void setAliasName(String aliasName) {
        String display = "";
        for (String namePart : aliasName.split(" ")) {
            display += namePart.substring(0, 1).toUpperCase();
        }
        authorText.setText(display);
    }

    private void setBgColor(int color) {
        ((GradientDrawable) background.getBackground()).setColor(color);
    }

    public void showUnreadMessageIndicator(boolean show) {
        unreadIndicator.setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void setTextSize(float authorTextSize) {
        // Descale dimension to actual value.
        // i.e. android will scale a 16sp dimen to 32 on a 2x density device, we want to
        // descale that back to 16.
        authorTextSize /= getResources().getDisplayMetrics().density;
        this.authorTextSize = authorTextSize;
        authorText.setTextSize(authorTextSize);
    }

    public void setTypeface(Typeface typeface) {
        authorText.setTypeface(typeface);
    }
}

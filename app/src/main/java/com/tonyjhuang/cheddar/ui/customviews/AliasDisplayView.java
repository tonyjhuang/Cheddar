package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.content.res.TypedArray;
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
 * YOU MUST SET THE STYLE TO R.style.AliasDisplay IN XML
 * SINCE YOU CANNOT UPDATE STYLES PROGRAMMATICALLY (for some reason).
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

    private float authorTextSize;

    @ViewById(R.id.background)
    View background;

    @ViewById(R.id.author_text)
    TextView authorText;

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
                ((GradientDrawable) background.getBackground()).setCornerRadius(getMeasuredHeight() / 2);
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @AfterViews
    public void afterViews() {
        if(authorTextSize == -1) {
            authorTextSize = defaultAuthorTextSize;
        }
        // Descale dimension to actual value.
        // i.e. android will scale a 16sp dimen to 32sp on a 2x density device, we want to
        // descale that back to 16.
        authorTextSize /= getResources().getDisplayMetrics().density;
        authorText.setTextSize(authorTextSize);
    }

    public void setAlias(Alias alias, boolean isCurrentUser) {
        setAliasName(alias.name());
        authorText.setTextColor(textColor);

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

    public void setBgColor(int color) {
        ((GradientDrawable) background.getBackground()).setColor(color);
    }
}

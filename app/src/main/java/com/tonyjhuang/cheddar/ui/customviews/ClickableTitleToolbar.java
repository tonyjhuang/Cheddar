package com.tonyjhuang.cheddar.ui.customviews;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Created by tonyjhuang on 3/25/16.
 */
public class ClickableTitleToolbar extends Toolbar {

    private TextView titleTextView;

    private TextView subtitleTextView;

    public ClickableTitleToolbar(Context context) {
        super(context);
    }

    public ClickableTitleToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableTitleToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public TextView getTitleTextView() {
        if (titleTextView == null) {
            try {
                titleTextView = (TextView) FieldUtils.readField(this, "mTitleTextView", true);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                Log.e("Toolbar", e.toString());
            }
        }

        return titleTextView;
    }

    public TextView getSubtitleTextView() {
        if (subtitleTextView == null) {
            try {
                subtitleTextView = (TextView) FieldUtils.readField(this, "mSubtitleTextView", true);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                Log.e("Toolbar", e.toString());
            }
        }

        return subtitleTextView;
    }
}

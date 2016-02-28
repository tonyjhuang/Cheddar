package com.tonyjhuang.chatly.api.models;

import android.content.Context;
import android.util.AttributeSet;

import com.tonyjhuang.chatly.ui.customviews.ImageOverlay;

/**
 * Created by tonyjhuang on 2/27/16.
 */
public class SendMessageImageOverlay extends ImageOverlay {

    public SendMessageImageOverlay(Context context) {
        super(context);
    }

    public SendMessageImageOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDisabled(boolean disabled) {
        if(disabled) {
            show(1, true);
        } else {
            hide(1, true);
        }
    }
}

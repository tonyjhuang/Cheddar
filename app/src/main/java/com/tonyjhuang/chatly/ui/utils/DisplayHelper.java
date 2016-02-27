package com.tonyjhuang.chatly.ui.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by tonyjhuang on 2/4/16.
 * Helper class for calculating dimensions around the display.
 */
public class DisplayHelper {
    public static int getScreenHeight(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
}

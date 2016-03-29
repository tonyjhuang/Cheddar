package com.tonyjhuang.cheddar.ui.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

/**
 * Created by tonyjhuang on 3/29/16.
 */
public class StringUtils {

    public static Spannable boldSubstring(String source, String target) {
        int targetIndex = source.indexOf(target);
        Spannable sb = new SpannableString(source);
        if (targetIndex != -1) {
            sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    targetIndex, targetIndex + target.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return sb;
    }
}

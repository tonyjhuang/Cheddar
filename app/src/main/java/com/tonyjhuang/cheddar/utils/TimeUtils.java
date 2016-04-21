package com.tonyjhuang.cheddar.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by tonyjhuang on 1/23/16.
 */
public class TimeUtils {

    public static final int SECOND = 1000;
    public static final int MINUTE = SECOND * 60;
    public static final int HOUR = MINUTE * 60;
    public static final int DAY = HOUR * 24;

    public static boolean isOlderThanBy(Date d1, Date d2, int diff) {
        return d1.getTime() - d2.getTime() > diff;
    }


    // 2016-02-25T22:39:24.435Z
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    private static final SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    static { sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC")); }
    public static Date getDateAsUTC(String dateString) {
        try {
            return sdfUTC.parse(dateString);
        } catch (ParseException e){
            Log.e("Time", e.toString());
            return new Date();
        }
    }
    public static String toString(Date date) {
        return sdf.format(date);
    }

}

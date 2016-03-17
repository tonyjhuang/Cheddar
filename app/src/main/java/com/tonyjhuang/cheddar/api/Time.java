package com.tonyjhuang.cheddar.api;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by tonyjhuang on 2/25/16.
 */
public class Time {

    // 2016-02-25T22:39:24.435Z
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    private static final SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    static { sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC")); }
    public static Date getDate(String dateString) {
        try {
            return sdf.parse(dateString);
        } catch (ParseException e){
            Log.e("Time", e.toString());
            return new Date();
        }
    }
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

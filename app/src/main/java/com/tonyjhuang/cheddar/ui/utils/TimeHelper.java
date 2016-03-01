package com.tonyjhuang.cheddar.ui.utils;

/**
 * Created by tonyjhuang on 1/23/16.
 */
public class TimeHelper {

    public static final int SECOND = 1000;
    public static final int MINUTE = SECOND * 60;
    public static final int HOUR = MINUTE * 60;
    public static final int DAY = HOUR * 24;

    public static int daysLeft(long duration) {
        return (int) duration / DAY;
    }

    public static int hoursLeft(long duration) {
        return (int) (duration % DAY) / HOUR;
    }

    public static int minutesLeft(long duration) {
        return (int) (duration % HOUR) / MINUTE;
    }

    public static int secondsLeft(long duration) {
        return (int) (duration % MINUTE) / SECOND;
    }

}

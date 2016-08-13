package com.tonyjhuang.cheddar.utils;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by tonyjhuang on 1/23/16.
 */
public class TimestampUtils {

    /**
     * Turn a date into its string representation. Rules:
     * - If a date is part of today, display hour:minute.
     * - If a date is was within six hours of now, display hour:minute.
     * - If a date is within 3 days of today, display the day of week.
     * - Otherwise, display month day
     */
    public static String formatDate(Date date, boolean addHourMinute) {
        // TODO: make this function not look like ass
        DateTime dateTime = new DateTime(date);
        DateTime midnight = new DateTime().withTimeAtStartOfDay();
        DateTime sixHoursAgo = new DateTime().minusHours(1);

        // 4:30 PM
        String hourMinute = removeLeadingZero(dateTime.toString("hh:mm a"));

        if (dateTime.isAfter(midnight) || dateTime.isAfter(sixHoursAgo)) {
            // 4:30 PM
            return hourMinute;
        } else {
            DateTime threeDaysAgo = midnight.minusDays(3);
            if (dateTime.isAfter(threeDaysAgo)) {
                if (addHourMinute) {
                    // Wed 4:30 PM
                    return dateTime.toString("EEE ") + hourMinute;
                } else {
                    // Wed
                    return dateTime.toString("EEE");
                }
            } else {
                if (addHourMinute) {
                    // Jan 7, 5:30am
                    return dateTime.toString("MMM ") +
                            removeLeadingZero(dateTime.toString("dd, ")) +
                            hourMinute;
                } else {
                    // Jan 7
                    return dateTime.toString("MMM ") +
                            removeLeadingZero(dateTime.toString("dd"));
                }

            }
        }
    }

    public static String formatDate(Date date) {
        return formatDate(date, false);
    }

    public static String removeLeadingZero(String string) {
        if (string.substring(0, 1).equals("0")) {
            string = string.substring(1, string.length());
        }
        return string;
    }
}

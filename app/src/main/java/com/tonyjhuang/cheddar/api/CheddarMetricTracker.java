package com.tonyjhuang.cheddar.api;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

/**
 * Tracks important app events.
 */
public class CheddarMetricTracker {

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    public static void trackJoinChatRoom(String chatRoomId) {
        Answers.getInstance().logCustom(new CustomEvent("Joined Chat")
                .putCustomAttribute("ChatRoom ID", chatRoomId));
    }

    public static void trackLeaveChatRoom(String chatRoomId, long lengthOfStay) {
        Answers.getInstance().logCustom(new CustomEvent("Left Chat")
                .putCustomAttribute("ChatRoom ID", chatRoomId)
                .putCustomAttribute("Length Of Stay", lengthOfStay));
    }

    public static void trackSendMessage(String chatRoomId, MessageLifecycle lifecycle) {
        Answers.getInstance().logCustom(new CustomEvent("Sent Message")
                .putCustomAttribute("ChatRoom ID", chatRoomId)
                .putCustomAttribute("Lifecycle", lifecycle.toString()));
    }

    public static void trackFeedback(FeedbackLifecycle lifecycle) {
        Answers.getInstance().logCustom(new CustomEvent("Feedback")
                .putCustomAttribute("Lifecycle", lifecycle.toString()));
    }

    public static void trackReportUser(ReportUserLifecycle lifecycle) {
        Answers.getInstance().logCustom(new CustomEvent("Report User")
                .putCustomAttribute("Lifecycle", lifecycle.toString()));
    }

    public enum MessageLifecycle {
        SENT, DELIVERED, FAILED
    }

    public enum FeedbackLifecycle {
        OPENED, SENT
    }

    public enum ReportUserLifecycle {
        CLICKED
    }

    private static String getBool(boolean b) {
        return b ? TRUE : FALSE;
    }
}

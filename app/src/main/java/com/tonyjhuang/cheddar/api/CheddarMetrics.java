package com.tonyjhuang.cheddar.api;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

/**
 * Tracks important app events.
 */
public class CheddarMetrics {

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

    public static void trackRegisterDifferentSchool(String email, String school) {
        Answers.getInstance().logCustom(new CustomEvent("Register Different School")
                .putCustomAttribute("Email", email)
                .putCustomAttribute("School", school));
    }

    public static void trackRegisterWithCode(String registrationCode) {
        Answers.getInstance().logCustom(new CustomEvent("Register With Code")
                .putCustomAttribute("Code", registrationCode));
    }

    public static void trackResetPassword(String email) {
        Answers.getInstance().logCustom(new CustomEvent("Reset Password")
                .putCustomAttribute("Email", email));
    }

    public static void trackLogout() {
        Answers.getInstance().logCustom(new CustomEvent("Logout"));
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
}

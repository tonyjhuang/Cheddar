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

    public static void trackLeaveChatRoom(String chatRoomId) {
        Answers.getInstance().logCustom(new CustomEvent("Left Chat")
                .putCustomAttribute("ChatRoom ID", chatRoomId));
    }

    public static void trackSendMessage(String chatRoomId, MessageLifecycle lifecycle) {
        Answers.getInstance().logCustom(new CustomEvent("Sent Message")
                .putCustomAttribute("ChatRoom ID", chatRoomId)
                .putCustomAttribute("Lifecycle", lifecycle.toString()));
    }

    public enum MessageLifecycle {
        SENT, DELIVERED, FAILED
    }

    private static String getBool(boolean b) {
        return b ? TRUE : FALSE;
    }
}

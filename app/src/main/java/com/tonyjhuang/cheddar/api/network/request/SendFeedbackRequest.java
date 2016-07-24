package com.tonyjhuang.cheddar.api.network.request;

/**
 * Created by tonyjhuang on 7/24/16.
 */
public class SendFeedbackRequest {
    String platform;
    String version;
    String build;
    String userId;
    String chatRoomId;
    String aliasName;
    String body;

    public SendFeedbackRequest(String platform,
                               String version,
                               String build,
                               String userId,
                               String chatRoomId,
                               String aliasName,
                               String body) {
        this.platform = platform;
        this.version = version;
        this.build = build;
        this.userId = userId;
        this.chatRoomId = chatRoomId;
        this.aliasName = aliasName;
        this.body = body;
    }
}

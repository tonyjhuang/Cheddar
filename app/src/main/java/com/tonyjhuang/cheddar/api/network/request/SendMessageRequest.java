package com.tonyjhuang.cheddar.api.network.request;

/**
 * Created by tonyjhuang on 4/22/16.
 */
public class SendMessageRequest {
    String aliasId;
    String messageId;
    String body;
    String subkey;
    String pubkey;

    public SendMessageRequest(String aliasId,
                              String messageId,
                              String body,
                              String subkey,
                              String pubkey) {
        this.aliasId = aliasId;
        this.messageId = messageId;
        this.body = body;
        this.subkey = subkey;
        this.pubkey = pubkey;
    }
}

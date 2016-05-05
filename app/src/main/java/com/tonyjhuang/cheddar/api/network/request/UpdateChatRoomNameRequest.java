package com.tonyjhuang.cheddar.api.network.request;

/**
 * Created by tonyjhuang on 5/4/16.
 */
public class UpdateChatRoomNameRequest {
    String aliasId;
    String name;
    String subkey;
    String pubkey;

    public UpdateChatRoomNameRequest(String aliasId, String name, String subkey, String pubkey) {
        this.aliasId = aliasId;
        this.name = name;
        this.subkey = subkey;
        this.pubkey = pubkey;
    }
}

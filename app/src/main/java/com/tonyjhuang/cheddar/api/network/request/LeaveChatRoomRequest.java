package com.tonyjhuang.cheddar.api.network.request;


public class LeaveChatRoomRequest {
    public String aliasId;
    public String subkey;
    public String pubkey;

    public LeaveChatRoomRequest(String aliasId, String subkey, String pubkey) {
        this.aliasId = aliasId;
        this.subkey = subkey;
        this.pubkey = pubkey;
    }
}

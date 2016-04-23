package com.tonyjhuang.cheddar.api.network.request;

/**
 * Created by tonyjhuang on 4/22/16.
 */
public class GetActiveAliasesRequest {
    public String chatRoomId;

    public GetActiveAliasesRequest(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}

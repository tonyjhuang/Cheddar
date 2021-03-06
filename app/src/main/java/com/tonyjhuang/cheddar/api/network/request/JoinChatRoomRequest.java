package com.tonyjhuang.cheddar.api.network.request;

/**
 * Created by tonyjhuang on 4/23/16.
 */
public class JoinChatRoomRequest {
    public String userId;
    public int maxOccupancy;
    public String topic;
    public String subkey;
    public String pubkey;

    public JoinChatRoomRequest(String userId, int maxOccupancy, String topic, String subkey, String pubkey) {
        this.userId = userId;
        this.maxOccupancy = maxOccupancy;
        this.topic = topic;
        this.subkey = subkey;
        this.pubkey = pubkey;
    }
}

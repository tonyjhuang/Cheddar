package com.tonyjhuang.cheddar.api.network.request;

/**
 * Created by tonyjhuang on 4/22/16.
 */
public class ReplayChatEventsRequest {
    public String aliasId;
    public int count;
    public String startTimeToken;
    public String endTimeToken;
    public String subkey;

    public ReplayChatEventsRequest(String aliasId, int count,
                                   String startTimeToken,
                                   String endTimeToken,
                                   String subkey) {
        this.aliasId = aliasId;
        this.count = count;
        this.startTimeToken = startTimeToken;
        this.endTimeToken = endTimeToken;
        this.subkey = subkey;
    }
}

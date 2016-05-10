package com.tonyjhuang.cheddar.api.network.request;

/**
 * Created by tonyjhuang on 4/22/16.
 */
public class ResendVerificationEmailRequest {
    public String userId;

    public ResendVerificationEmailRequest(String userId) {
        this.userId = userId;
    }
}

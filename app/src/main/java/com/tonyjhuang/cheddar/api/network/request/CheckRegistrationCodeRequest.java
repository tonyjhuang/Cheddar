package com.tonyjhuang.cheddar.api.network.request;

/**
 * Created by tonyjhuang on 7/24/16.
 */
public class CheckRegistrationCodeRequest {
    String registrationCode;

    public CheckRegistrationCodeRequest(String registrationCode) {
        this.registrationCode = registrationCode;
    }
}

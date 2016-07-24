package com.tonyjhuang.cheddar.api.network.request;

/**
 * Created by tonyjhuang on 7/24/16.
 */
public class SendChangeSchoolRequest {
    String platform;
    String schoolName;
    String email;

    public SendChangeSchoolRequest(String platform, String schoolName, String email) {
        this.platform = platform;
        this.schoolName = schoolName;
        this.email = email;
    }
}

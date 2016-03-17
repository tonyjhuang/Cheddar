package com.tonyjhuang.cheddar.api.feedback;

import org.json.JSONObject;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by tonyjhuang on 3/17/16.
 */
public interface FeedbackService {
    @POST("T0NCAPM7F/B0TEWG8PP/PHH9wkm2DCq6DlUdgLZvepAQ")
    Observable<String> sendFeedback(@Body FeedbackRequest body);
}

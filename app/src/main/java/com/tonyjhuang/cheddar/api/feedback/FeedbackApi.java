package com.tonyjhuang.cheddar.api.feedback;

import android.util.Log;

import org.androidannotations.annotations.EBean;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by tonyjhuang on 3/17/16.
 * Api for sending user feedback to the team.
 */
@EBean(scope = EBean.Scope.Singleton)
public class FeedbackApi {

    private static final String TAG = FeedbackApi.class.getSimpleName();
    private static final String BASE_URL = "https://hooks.slack.com/services/";

    FeedbackService service;

    public FeedbackApi() {
        // Logging
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        service = retrofit.create(FeedbackService.class);
    }

    /**
     * chatRoomId is optional.
     */
    public Observable<String> sendFeedback(String userId, String chatRoomId, String feedback) {
        String chatRoomText = chatRoomId != null ? " in ChatRoom(" + chatRoomId + ")" : "";
        String text = String.format("User(%s)%s: %s", userId, chatRoomText, feedback);

        FeedbackRequest request = new FeedbackRequest(text);
        return service.sendFeedback(request);
    }
}

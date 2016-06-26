package com.tonyjhuang.cheddar.api.feedback;

import org.androidannotations.annotations.EBean;

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
     * All string args are optional.
     */
    public Observable<String> sendFeedback(FeedbackInfo info) {
        FeedbackRequest request = new FeedbackRequest(info.toString());
        return service.sendFeedback(request);
    }

    public static class FeedbackInfo {
        String versionName = "???";
        String userId;
        String aliasName;
        String chatRoomId;
        String name = "Anonymous";
        String feedback;

        public String toString() {
            StringBuilder sb = new StringBuilder(String.format("\nVersionName: %s\n", versionName));
            if (userId != null) sb.append(String.format("User Id: %s\n", userId));
            if (chatRoomId != null) sb.append(String.format("ChatRoom Id: %s\n", chatRoomId));
            if (aliasName != null) sb.append(String.format("Alias name: %s\n", aliasName));
            if (feedback != null) sb.append(String.format("%s: %s\n", name, feedback));
            return sb.toString();
        }

        public static class Builder {
            private FeedbackInfo info;

            public Builder() {
                this.info = new FeedbackInfo();
            }

            public FeedbackInfo build() {
                return info;
            }

            public Builder setVersionName(String versionName) {
                info.versionName = versionName;
                return this;
            }

            public Builder setUserId(String userId) {
                info.userId = userId;
                return this;
            }

            public Builder setAliasName(String aliasName) {
                info.aliasName = aliasName;
                return this;
            }

            public Builder setChatRoomId(String chatRoomId) {
                info.chatRoomId = chatRoomId;
                return this;
            }

            public Builder setName(String name) {
                info.name = (name == null || name.isEmpty()) ? "Anonymous" : name;
                return this;
            }

            public Builder setFeedback(String feedback) {
                info.feedback = feedback;
                return this;
            }
        }
    }
}

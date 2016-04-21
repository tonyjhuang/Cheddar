package com.tonyjhuang.cheddar.api.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.api.CheddarGson;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.network.request.GetChatRoomsRequest;
import com.tonyjhuang.cheddar.presenter.Scheduler;

import org.androidannotations.annotations.EBean;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

@EBean(scope = EBean.Scope.Singleton)
public class ParseApi {
    private static final String BASE_URL = "https://api.parse.com/1/functions/";
    private final ParseService service;

    public ParseApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(createGson()))
                .client(createHttpClient())
                .baseUrl(BASE_URL)
                .build();

        service = retrofit.create(ParseService.class);
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(CheddarGson.typeAdapterFactory())
                .create();
    }

    private static OkHttpClient createHttpClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            Request request = original.newBuilder()
                    .header("Content-ChatEventType", "application/json")
                    .header("X-Parse-Application-Id", BuildConfig.PARSE_APPKEY)
                    .header("X-Parse-REST-API-Key", BuildConfig.PARSE_RSTKEY)
                    .method(original.method(), original.body())
                    .build();

            return chain.proceed(request);
        });
        return httpClient.build();
    }

    public Observable<String> test() {
        return service.test().compose(Scheduler.backgroundSchedulers());
    }

    /**
     * Get the list of ChatRooms, Aliases, and their most recent ChatEvents
     * for this user.
     */
    public Observable<List<ChatRoomInfo>> getChatRooms(String userId) {
        return service.getChatRooms(GetChatRoomsRequest.create(userId))
                .compose(Scheduler.backgroundSchedulers());
    }
}

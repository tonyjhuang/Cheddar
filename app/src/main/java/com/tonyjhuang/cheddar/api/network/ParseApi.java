package com.tonyjhuang.cheddar.api.network;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.network.request.FindAliasRequest;
import com.tonyjhuang.cheddar.api.network.request.GetChatRoomsRequest;
import com.tonyjhuang.cheddar.api.network.request.ReplayChatEventsRequest;
import com.tonyjhuang.cheddar.api.network.response.replaychatevent.ReplayChatEventDeserializer;
import com.tonyjhuang.cheddar.api.network.response.replaychatevent.ReplayChatEventObjectHolder;
import com.tonyjhuang.cheddar.api.network.response.replaychatevent.ReplayChatEventsResponse;
import com.tonyjhuang.cheddar.presenter.Scheduler;

import org.androidannotations.annotations.EBean;

import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

import static com.tonyjhuang.cheddar.api.MessageApi.SUBKEY;

@EBean(scope = EBean.Scope.Singleton)
public class ParseApi {
    /**
     * Endpoints.
     */
    private static final String BASE_URL = "https://api.parse.com/1/functions/";
    /**
     * Retrofit service.
     */
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
                .registerTypeAdapterFactory(new ResultUnwrapperTypeAdapterFactory())
                .registerTypeAdapter(ReplayChatEventObjectHolder.class, new ReplayChatEventDeserializer())
                .create();
    }

    private static OkHttpClient createHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

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
        httpClient.addInterceptor(loggingInterceptor);
        return httpClient.build();
    }

    public Observable<String> test() {
        return service.test().compose(Scheduler.backgroundSchedulers());
    }

    /**
     * Gets the Alias for the given id.
     */
    public Observable<Alias> findAlias(String aliasId) {
        return service.findAlias(new FindAliasRequest(aliasId));
    }

    /**
     * Get the list of ChatRooms, Aliases, and their most recent ChatEvents
     * for this user.
     */
    public Observable<List<ChatRoomInfo>> getChatRooms(String userId) {
        return service.getChatRooms(new GetChatRoomsRequest(userId));
    }

    /**
     * Pubnub returns tokens in the form of nanoseconds from epoch.
     * Let's transform those into more manageable/modular dates.
     */
    private Date tokenToDate(String token) {
        return new Date(Long.valueOf(token) / 10000);
    }

    /**
     * See #tokenToDate.
     */
    private String dateToToken(@Nullable Date date) {
        if(date == null) return null;
        return date.getTime() * 10000 + "";
    }

    /**
     * Get a page of the ChatEvent history. The response contains
     * paging tokens "startTimeToken" and "endTimeToken" to go
     * further back in history.
     */
    public Observable<ChatEventPage> pageChatEvents(String aliasId, int count, @Nullable Date start) {
        ReplayChatEventsRequest request = new ReplayChatEventsRequest(
                aliasId, count, dateToToken(start), null, SUBKEY);

        return service.replayChatEvents(request)
                .map(response -> new ChatEventPage(tokenToDate(response.startTimeToken),
                        tokenToDate(response.endTimeToken), response.getChatEvents()));
    }

    /**
     * Get all ChatEvents that were sent in a time range.
     */
    public Observable<List<ChatEvent>> getChatEventsInRange(String aliasId, Date start, Date end) {
        ReplayChatEventsRequest request = new ReplayChatEventsRequest(
                aliasId, 9999, dateToToken(start), dateToToken(end), SUBKEY);

        return service.replayChatEvents(request)
                .map(ReplayChatEventsResponse::getChatEvents);
    }
}

package com.tonyjhuang.cheddar.api.network;

import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.parse.ParseUser;
import com.tonyjhuang.cheddar.BuildConfig;
import com.tonyjhuang.cheddar.api.CheddarMetrics;
import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoom;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.value.MetaData;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.api.network.request.CheckRegistrationCodeRequest;
import com.tonyjhuang.cheddar.api.network.request.FindAliasRequest;
import com.tonyjhuang.cheddar.api.network.request.FindChatRoomRequest;
import com.tonyjhuang.cheddar.api.network.request.FindUserRequest;
import com.tonyjhuang.cheddar.api.network.request.GetActiveAliasesRequest;
import com.tonyjhuang.cheddar.api.network.request.GetChatRoomsRequest;
import com.tonyjhuang.cheddar.api.network.request.JoinChatRoomRequest;
import com.tonyjhuang.cheddar.api.network.request.LeaveChatRoomRequest;
import com.tonyjhuang.cheddar.api.network.request.ReplayChatEventsRequest;
import com.tonyjhuang.cheddar.api.network.request.ResendVerificationEmailRequest;
import com.tonyjhuang.cheddar.api.network.request.SendChangeSchoolRequest;
import com.tonyjhuang.cheddar.api.network.request.SendFeedbackRequest;
import com.tonyjhuang.cheddar.api.network.request.SendMessageRequest;
import com.tonyjhuang.cheddar.api.network.request.UpdateChatRoomNameRequest;
import com.tonyjhuang.cheddar.api.network.response.replaychatevent.ChatEventPage;
import com.tonyjhuang.cheddar.api.network.response.replaychatevent.ReplayChatEventDeserializer;
import com.tonyjhuang.cheddar.api.network.response.replaychatevent.ReplayChatEventObjectHolder;
import com.tonyjhuang.cheddar.api.network.response.replaychatevent.ReplayChatEventsResponse;

import org.androidannotations.annotations.EBean;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

import static com.tonyjhuang.cheddar.api.message.MessageApi.PUBKEY;
import static com.tonyjhuang.cheddar.api.message.MessageApi.SUBKEY;

@EBean(scope = EBean.Scope.Singleton)
public class ParseApi {
    /**
     * The objects from the network are in UTC time. We need to parse
     * incoming dates respecting that, otherwise they'll be parsed
     * as whatever the local timezone is, which is WRONG!!!
     */
    private static final DateFormat utcDateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    /**
     * Endpoints.
     */
    private static final String BASE_URL = "https://api.parse.com/1/functions/";
    /**
     * Number of users for a typical group chat.
     */
    private static final int GROUP_OCCUPANCY = 5;

    /**
     * For sending user feedback.
     */
    private static final String PLATFORM = "Android";

    static {
        utcDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


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
                .registerTypeAdapter(Date.class, new UtcDateAdapter())
                .registerTypeAdapter(ChatEvent.ChatEventType.class, ChatEvent.ChatEventType.DESERIALIZER)
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

    public Observable<Boolean> checkRegistrationCode(String registrationCode) {
        return service.checkRegistrationCode(new CheckRegistrationCodeRequest(registrationCode));
    }

    public Observable<User> findUser(String userId) {
        return service.findUser(new FindUserRequest(userId));
    }

    public Observable<Boolean> isUserEmailVerified(String userId) {
        return findUser(userId).map(User::emailVerified);
    }

    public Observable<User> resendVerificationEmail(String userId) {
        return service.resendVerificationEmail(new ResendVerificationEmailRequest(userId));
    }

    /**
     * DEBUG.
     * Deletes the current logged in user.
     */
    public Observable<Void> deleteCurrentUser() {
        return Observable.defer(() -> {
            try {
                ParseUser.getCurrentUser().delete();
                return Observable.just(null);
            } catch (com.parse.ParseException e) {
                return Observable.error(e);
            }
        });
    }

    /**
     * Registers a new user with the server.
     */
    public Observable<User> registerUser(String email, String password, String registrationCode) {
        Observable<User> registerNewUserObservable = Observable.create(subscriber -> {
            ParseUser.logOut();
            ParseUser user = new ParseUser();
            user.setUsername(email);
            user.setPassword(password);
            user.setEmail(email);
            user.put("registrationCode", registrationCode == null ? "" : registrationCode);

            user.signUpInBackground(error -> {
                if (subscriber.isUnsubscribed()) return;
                if (error == null) {
                    subscriber.onNext(toUser(user));
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(error);
                }
            });
        });

        return Observable.defer(() -> {
            if (registrationCode == null) {
                // If no registration code was passed in, try to register user.
                return registerNewUserObservable;
            } else {
                // Otherwise, check the registration code against the server and only register
                // if it's valid.
                return checkRegistrationCode(registrationCode)
                        .flatMap(isValidCode -> {
                            if (isValidCode) {
                                return registerNewUserObservable;
                            } else {
                                return Observable.error(new InvalidRegistrationCodeException(registrationCode));
                            }
                        });
            }
        }).doOnError(Crashlytics::logException);
    }

    public Observable<User> login(String email, String password) {
        return Observable.create(subscriber -> {
            ParseUser.logInInBackground(email, password, (user, error) -> {
                if (subscriber.isUnsubscribed()) return;
                if (user != null) {
                    subscriber.onNext(toUser(user));
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(error);
                }
            });
        });
    }

    public Observable<Void> logout() {
        return Observable.defer(() -> {
            ParseUser.logOut();
            return Observable.just(null);
        });
    }

    /**
     * Translate a ParseUser into a our value type User.
     */
    private User toUser(ParseUser parseUser) {
        MetaData metaData = MetaData.create(
                parseUser.getObjectId(), parseUser.getCreatedAt(), parseUser.getUpdatedAt());
        return User.create(metaData, parseUser.getUsername(), parseUser.getBoolean("emailVerified"));
    }

    /**
     * Gets the Alias for the given id.
     */
    public Observable<Alias> findAlias(String aliasId) {
        return service.findAlias(new FindAliasRequest(aliasId));
    }

    /**
     * Gets the active Aliases for the ChatRoom with the given id.
     */
    public Observable<List<Alias>> getActiveAliases(String chatRoomId) {
        return service.getActiveAliases(new GetActiveAliasesRequest(chatRoomId));
    }

    public Observable<ChatRoom> findChatRoom(String chatRoomId) {
        return service.findChatRoom(new FindChatRoomRequest(chatRoomId));
    }

    /**
     * Places the User into a new group ChatRoom.
     */
    public Observable<Alias> joinGroupChatRoom(String userId) {
        JoinChatRoomRequest request =
                new JoinChatRoomRequest(userId, GROUP_OCCUPANCY, SUBKEY, PUBKEY);
        return service.joinChatRoom(request);
    }

    public Observable<Alias> leaveChatRoom(String aliasId) {
        LeaveChatRoomRequest request =
                new LeaveChatRoomRequest(aliasId, SUBKEY, PUBKEY);
        return service.leaveChatRoom(request);
    }

    /**
     * Get the list of ChatRooms, Aliases, and their most recent ChatEvents
     * for this user.
     */
    public Observable<List<ChatRoomInfo>> getChatRooms(String userId) {
        return service.getChatRooms(new GetChatRoomsRequest(userId));
    }

    /**
     * Update a ChatRoom's name.
     */
    public Observable<ChatRoom> updateChatRoomName(String aliasId, String name) {
        return service.updateChatRoomName(new UpdateChatRoomNameRequest(aliasId, name, SUBKEY, PUBKEY));
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
        if (date == null) return null;
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

    public Observable<ChatEvent> sendMessage(String aliasId, String body, @Nullable String messageId) {
        SendMessageRequest request =
                new SendMessageRequest(aliasId, messageId, body, SUBKEY, PUBKEY);

        return service.sendMessage(request);
    }

    public Observable<String> sendFeedback(Alias alias, String feedback) {
        SendFeedbackRequest request = new SendFeedbackRequest(
                PLATFORM, // Platform
                BuildConfig.VERSION_NAME, // Version
                Integer.toString(BuildConfig.VERSION_CODE), // Build
                alias.userId(),
                alias.chatRoomId(),
                alias.name(),
                feedback);
        return service.sendFeedback(request);
    }

    public Observable<String> sendChangeSchoolRequest(String schoolName, String email) {
        return service.sendChangeSchoolRequest(new SendChangeSchoolRequest(PLATFORM, schoolName, email));
    }

    /**
     * Parse Dates as Utc (respect local timezone offset).
     */
    private static class UtcDateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return utcDateFormat.parse(json.getAsString());
            } catch (ParseException e) {
                throw new JsonParseException(e);
            }
        }

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(utcDateFormat.format(src));
        }
    }

    public static class InvalidRegistrationCodeException extends Exception {
        private String registrationCode;

        public InvalidRegistrationCodeException(String registrationCode) {
            this.registrationCode = registrationCode;
        }

        @Override
        public String getMessage() {
            return "Invalid registration code: \"" + registrationCode + "\"";
        }
    }
}

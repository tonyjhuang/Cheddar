package com.tonyjhuang.cheddar.api.network;

import com.tonyjhuang.cheddar.api.models.value.Alias;
import com.tonyjhuang.cheddar.api.models.value.ChatEvent;
import com.tonyjhuang.cheddar.api.models.value.ChatRoom;
import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.models.value.User;
import com.tonyjhuang.cheddar.api.network.request.FindAliasRequest;
import com.tonyjhuang.cheddar.api.network.request.FindChatRoomRequest;
import com.tonyjhuang.cheddar.api.network.request.GetActiveAliasesRequest;
import com.tonyjhuang.cheddar.api.network.request.GetChatRoomsRequest;
import com.tonyjhuang.cheddar.api.network.request.JoinChatRoomRequest;
import com.tonyjhuang.cheddar.api.network.request.LeaveChatRoomRequest;
import com.tonyjhuang.cheddar.api.network.request.ReplayChatEventsRequest;
import com.tonyjhuang.cheddar.api.network.request.SendMessageRequest;
import com.tonyjhuang.cheddar.api.network.request.UpdateChatRoomNameRequest;
import com.tonyjhuang.cheddar.api.network.response.replaychatevent.ReplayChatEventsResponse;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

/**
 * API for our Parse network backend.
 */
public interface ParseService {

    @POST("hello")
    Observable<String> test();

    @POST("registerNewUser")
    Observable<User> registerNewUser();

    @POST("findAlias")
    Observable<Alias> findAlias(@Body FindAliasRequest body);

    @POST("getActiveAliases")
    Observable<List<Alias>> getActiveAliases(@Body GetActiveAliasesRequest body);

    @POST("replayEvents")
    Observable<ReplayChatEventsResponse> replayChatEvents(@Body ReplayChatEventsRequest body);

    @POST("sendMessage")
    Observable<ChatEvent> sendMessage(@Body SendMessageRequest body);

    @POST("findChatRoom")
    Observable<ChatRoom> findChatRoom(@Body FindChatRoomRequest body);

    @POST("getChatRooms")
    Observable<List<ChatRoomInfo>> getChatRooms(@Body GetChatRoomsRequest body);

    @POST("joinNextAvailableChatRoom")
    Observable<Alias> joinChatRoom(@Body JoinChatRoomRequest body);

    @POST("leaveChatRoom")
    Observable<Alias> leaveChatRoom(@Body LeaveChatRoomRequest body);

    @POST("updateChatRoomName")
    Observable<ChatRoom> updateChatRoomName(@Body UpdateChatRoomNameRequest body);
}

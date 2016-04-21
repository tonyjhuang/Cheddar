package com.tonyjhuang.cheddar.api.network;

import com.tonyjhuang.cheddar.api.models.value.ChatRoomInfo;
import com.tonyjhuang.cheddar.api.network.request.GetChatRoomsRequest;

import java.util.List;

import retrofit2.http.POST;
import rx.Observable;

/**
 * API for our Parse network backend.
 */
public interface ParseService {

    @POST("hello")
    Observable<String> test();

    @POST("getChatRooms")
    Observable<List<ChatRoomInfo>> getChatRooms(GetChatRoomsRequest request);
}

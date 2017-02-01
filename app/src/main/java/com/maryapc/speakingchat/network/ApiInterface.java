package com.maryapc.speakingchat.network;

import com.maryapc.speakingchat.model.brooadcast.BroadcastResponse;
import com.maryapc.speakingchat.model.chat.ChatResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface ApiInterface {

	@GET(GoogleApiUrls.Youtube.BROADCAST)
	Observable<BroadcastResponse> getBroadcast(@Query("part") String part, @Query("broadcastStatus") String status, @Query("broadcastType") String type);

	@GET(GoogleApiUrls.Youtube.CHAT)
	Observable<ChatResponse> getChat(@Query("liveChatId") String liveChatId, @Query("part") String part);
}

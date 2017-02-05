package com.maryapc.speakingchat.network;

import com.maryapc.speakingchat.model.TokenResponse;
import com.maryapc.speakingchat.model.brooadcast.BroadcastResponse;
import com.maryapc.speakingchat.model.chat.ChatResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;

public interface ApiInterface {

	@GET(GoogleApiUrls.Youtube.BROADCAST)
	Observable<BroadcastResponse> getBroadcast(@Header("Authorization") String token, @Query("part") String part, @Query("broadcastStatus") String status, @Query("broadcastType") String type);

	@GET(GoogleApiUrls.Youtube.CHAT)
	Observable<ChatResponse> getChat(@Header("Authorization") String token, @Query("liveChatId") String liveChatId, @Query("part") String part);

	@GET(GoogleApiUrls.OAuth.TOKEN_INFO_URL)
	Call<TokenResponse> getTokenInfo(@Query("access_token") String accessToken);
}

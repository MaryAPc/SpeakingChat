package com.maryapc.speakingchat.network;

import com.maryapc.speakingchat.MyApplication;
import com.maryapc.speakingchat.R;

public class GoogleApiUrls {

	static final String BASE_URL = "https://www.googleapis.com";
	static final String YOUTUBE_URL = BASE_URL + "/youtube/v3";
	static final String OAUTH_URL = BASE_URL + "/oauth2/v3";

	public static class Youtube {

		static final String CHAT = YOUTUBE_URL + "/liveChat/messages";
		static final String BROADCAST = YOUTUBE_URL + "/liveBroadcasts";
	}

	public static class OAuth {

		static final String TOKEN_INFO_URL =  OAUTH_URL + "/tokeninfo";
	}

	public static String getSignInUrl() {
		String client_id = MyApplication.getInstance().getString(R.string.android_client_id);
		String redirect_uri = "urn:ietf:wg:oauth:2.0:oob";
		String access_type = "offline";
		String response_type = "code";
		String scope = "https://www.googleapis.com/auth/youtube.readonly";
		String endPoint = "https://accounts.google.com/o/oauth2/auth";

		return endPoint + "?" + "client_id=" + client_id + "&redirect_uri=" + redirect_uri
		       + "&access_type=" + access_type + "&response_type=" + response_type + "&scope=" + scope;
	}
}

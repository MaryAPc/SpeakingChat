package com.maryapc.speakingchat.network;

public class GoogleApiUrls {

	static final String BASE_URL = "https://www.googleapis.com";
	static final String YOUTUBE_URL = BASE_URL + "/youtube/v3";

	public static class Youtube {

		static final String CHAT = YOUTUBE_URL + "/liveChat/messages";
		static final String BROADCAST = YOUTUBE_URL + "/liveBroadcasts";
	}
}

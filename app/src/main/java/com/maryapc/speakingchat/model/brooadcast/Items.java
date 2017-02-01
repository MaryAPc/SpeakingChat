package com.maryapc.speakingchat.model.brooadcast;

import com.google.gson.annotations.SerializedName;

public class Items {

	@SerializedName("kind")
	private String kind;
	@SerializedName("id")
	private String id;
	@SerializedName("snippet")
	private Snippet snippet;

	public String getKind() {
		return kind;
	}

	public String getId() {
		return id;
	}

	public Snippet getSnippet() {
		return snippet;
	}

	public static class Snippet {
		@SerializedName("channelId")
		private String channelId;
		@SerializedName("title")
		private String title;
		@SerializedName("liveChatId")
		private String liveChatId;

		public String getChannelId() {
			return channelId;
		}

		public String getTitle() {
			return title;
		}

		public String getLiveChatId() {
			return liveChatId;
		}
	}
}

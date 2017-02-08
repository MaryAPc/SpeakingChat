package com.maryapc.speakingchat.model.brooadcast;

import com.google.gson.annotations.SerializedName;

public class ItemsBroadcast {

	@SerializedName("kind")
	private String mKind;
	@SerializedName("id")
	private String mIdBroadcast;
	@SerializedName("snippet")
	private Snippet mSnippet;

	public String getKind() {
		return mKind;
	}

	//id трансляции
	public String getId() {
		return mIdBroadcast;
	}

	public Snippet getSnippet() {
		return mSnippet;
	}

	public static class Snippet {
		@SerializedName("channelId") //id канала
		private String mChannelId;
		@SerializedName("title") //название трансляции
		private String mTitle;
		@SerializedName("liveChatId") //id чата (для запроса)
		private String mLiveChatId;

		public String getChannelId() {
			return mChannelId;
		}

		public String getTitle() {
			return mTitle;
		}

		public String getLiveChatId() {
			return mLiveChatId;
		}
	}
}

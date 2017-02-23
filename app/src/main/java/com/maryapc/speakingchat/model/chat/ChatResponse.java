package com.maryapc.speakingchat.model.chat;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {

	@SerializedName("nextPageToken")
	private String mNextPageToken;
	@SerializedName("pageInfo")
	private PageInfo mPageInfo;
	@SerializedName("items")
	private List<ItemsChat> mItemsChat;
	@SerializedName("pollingIntervalMillis")
	private int mPollingIntervalMillis;

	public String getNextPageToken() {
		return mNextPageToken;
	}

	public PageInfo getPageInfo() {
		return mPageInfo;
	}

	public List<ItemsChat> getItems() {
		return mItemsChat;
	}

	public int getPollingIntervalMillis() {
		return mPollingIntervalMillis;
	}

	public static class PageInfo {
		@SerializedName("totalResults")
		private String mTotalResults;
		@SerializedName("resultsPerPage")
		private String mResultsPerPage;

		public String getTotalResults() {
			return mTotalResults;
		}

		public String getResultsPerPage() {
			return mResultsPerPage;
		}
	}
}

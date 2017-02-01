package com.maryapc.speakingchat.model.chat;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {

	@SerializedName("nextPageToken")
	private String nextPageToken;
	@SerializedName("pageInfo")
	private PageInfo pageInfo;
	@SerializedName("items")
	private List<Items> items;

	public String getNextPageToken() {
		return nextPageToken;
	}

	public PageInfo getPageInfo() {
		return pageInfo;
	}

	public List<Items> getItems() {
		return items;
	}

	public static class PageInfo {
		@SerializedName("totalResults")
		private String totalResults;
		@SerializedName("resultsPerPage")
		private String resultsPerPage;

		public String getTotalResults() {
			return totalResults;
		}

		public String getResultsPerPage() {
			return resultsPerPage;
		}
	}
}

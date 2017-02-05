package com.maryapc.speakingchat.model.brooadcast;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class BroadcastResponse {

	@SerializedName("kind")
	private String mKind;
	@SerializedName("etag")
	private String mEtag;
	@SerializedName("items")
	private List<ItemsBroadcast> mItems;

	public String getKind() {
		return mKind;
	}

	public String getEtag() {
		return mEtag;
	}

	public List<ItemsBroadcast> getItems() {
		return mItems;
	}
}

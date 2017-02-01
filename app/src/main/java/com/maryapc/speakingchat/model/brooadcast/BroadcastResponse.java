package com.maryapc.speakingchat.model.brooadcast;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.maryapc.speakingchat.model.chat.Items;

public class BroadcastResponse {


	@SerializedName("kind")
	private String kind;
	@SerializedName("etag")
	private String etag;
	@SerializedName("items")
	private List<Items> items;

	public String getKind() {
		return kind;
	}

	public String getEtag() {
		return etag;
	}

	public List<Items> getItems() {
		return items;
	}
}

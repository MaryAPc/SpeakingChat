package com.maryapc.speakingchat.model;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {

	@SerializedName("error_description")
	private String mError;

	public String getError() {
		return mError;
	}
}

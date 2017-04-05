package com.maryapc.speakingchat.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

public class User extends BaseObservable {

	private String mAvatar;
	private String mUsername;
	private String mEmail;

	public User(String avatar, String username, String email) {
		mAvatar = avatar;
		mUsername = username;
		mEmail = email;
	}

	@Bindable
	public String getEmail() {
		return mEmail;
	}

	@Bindable
	public String getUsername() {
		return mUsername;
	}

	@Bindable
	public String getAvatar() {
		return mAvatar;
	}
}

package com.maryapc.speakingchat.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {

	private static final String APP_PREFERENCES = "app_preferences";
	private static final String SIGN_IN = "sign_in";
	private static final String UPDATE_SIGN = "update_sign";
	private static final String FIRST_LAUNCH = "first_launch";
	private static final String USER_AVATAR = "user_avatar";
	private static final String USER_NAME = "user_name";
	private static final String USER_EMAIL = "user_email";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String TOKEN_TYPE = "token_type";

	private final SharedPreferences sSharedPreferences;

	public AppPreferences(Context context) {
		sSharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
	}

	public boolean isSignIn() {
		return sSharedPreferences.getBoolean(SIGN_IN, false);
	}

	public void setSignIn(boolean value) {
		sSharedPreferences.edit().putBoolean(SIGN_IN, value).apply();
	}

	public boolean isUpdateSignIn() {
		return sSharedPreferences.getBoolean(UPDATE_SIGN, false);
	}

	public void setUpdateSignIn(boolean value) {
		sSharedPreferences.edit().clear().putBoolean(UPDATE_SIGN, value).apply();
	}

	public boolean isFirstLaunch() {
		return sSharedPreferences.getBoolean(FIRST_LAUNCH, false);
	}

	public void setFirstLaunch(boolean value) {
		sSharedPreferences.edit().putBoolean(FIRST_LAUNCH, value).apply();
	}

	public String getUserAvatar() {
		return sSharedPreferences.getString(USER_AVATAR, "");
	}

	public void saveUserAvatar(String value) {
		sSharedPreferences.edit().putString(USER_AVATAR, value).apply();
	}

	public String getUserName() {
		return sSharedPreferences.getString(USER_NAME, "");
	}

	public void saveUserName(String value) {
		sSharedPreferences.edit().putString(USER_NAME, value).apply();
	}

	public String getUserEmail() {
		return sSharedPreferences.getString(USER_EMAIL, "");
	}

	public void saveUserEmail(String value) {
		sSharedPreferences.edit().putString(USER_EMAIL, value).apply();
	}

	public String getRefreshToken() {
		return sSharedPreferences.getString(REFRESH_TOKEN, "");
	}

	public void saveRefreshToken(String value) {
		sSharedPreferences.edit().putString(REFRESH_TOKEN, value).apply();
	}

	public String getAccessToken() {
		return sSharedPreferences.getString(ACCESS_TOKEN, "");
	}

	public void saveAccessToken(String value) {
		sSharedPreferences.edit().putString(ACCESS_TOKEN, value).apply();
	}

	public String getTokenType() {
		return sSharedPreferences.getString(TOKEN_TYPE, "");
	}

	public void saveTokenType(String value) {
		sSharedPreferences.edit().putString(TOKEN_TYPE, value).apply();
	}
}

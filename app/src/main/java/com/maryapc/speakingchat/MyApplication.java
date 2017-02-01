package com.maryapc.speakingchat;

import com.arellomobile.mvp.MvpApplication;

public class MyApplication extends MvpApplication {

	public static MyApplication sInstance;

	public static MyApplication getInstance() {
		return sInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
	}
}
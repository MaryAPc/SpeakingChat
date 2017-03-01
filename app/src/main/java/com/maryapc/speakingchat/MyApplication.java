package com.maryapc.speakingchat;

import com.arellomobile.mvp.MvpApplication;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class MyApplication extends MvpApplication {

	public static MyApplication sInstance;

	public static MyApplication getInstance() {
		return sInstance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		sInstance = this;
	}
}
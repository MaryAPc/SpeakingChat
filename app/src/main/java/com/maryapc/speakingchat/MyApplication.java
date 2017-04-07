package com.maryapc.speakingchat;

import com.arellomobile.mvp.MvpApplication;
import com.crashlytics.android.Crashlytics;
import com.maryapc.speakingchat.di.AppComponent;
import com.maryapc.speakingchat.di.DaggerAppComponent;
import com.maryapc.speakingchat.di.module.AppContextModule;
import com.maryapc.speakingchat.di.module.PreferencesModule;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends MvpApplication {

	private static AppComponent sAppComponent;
	public static MyApplication sInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		sInstance = this;

		sAppComponent = DaggerAppComponent.builder()
				.appContextModule(new AppContextModule(this))
				.preferencesModule(new PreferencesModule())
				.build();
	}

	public static MyApplication getInstance() {
		return sInstance;
	}

	public static AppComponent getAppComponent() {
		return sAppComponent;
	}
}
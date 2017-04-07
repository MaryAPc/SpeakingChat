package com.maryapc.speakingchat.di.module;

import android.content.Context;

import com.maryapc.speakingchat.utils.AppPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PreferencesModule {

	@Provides
	@Singleton
	AppPreferences providesAppPreferences(Context context) {
		return new AppPreferences(context);
	}
}

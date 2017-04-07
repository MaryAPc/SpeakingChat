package com.maryapc.speakingchat.di;

import com.maryapc.speakingchat.ChatListActivity;
import com.maryapc.speakingchat.di.module.AppContextModule;
import com.maryapc.speakingchat.di.module.PreferencesModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppContextModule.class, PreferencesModule.class})
public interface AppComponent {

	void inject(ChatListActivity activity);
}

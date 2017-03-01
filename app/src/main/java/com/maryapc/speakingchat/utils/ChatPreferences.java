package com.maryapc.speakingchat.utils;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.maryapc.speakingchat.R;

public class ChatPreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}

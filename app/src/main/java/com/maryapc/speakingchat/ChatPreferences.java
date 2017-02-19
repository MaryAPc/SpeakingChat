package com.maryapc.speakingchat;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ChatPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
}

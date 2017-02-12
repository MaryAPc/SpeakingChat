package com.maryapc.speakingchat;

import android.speech.tts.TextToSpeech;

import com.maryapc.speakingchat.adapter.recycler.ChatListAdapter;
import com.maryapc.speakingchat.presenter.ChatListPresenter;

public class SpeakService {

	public static SpeechStatus mStatus;

	public synchronized static void speechMessages(TextToSpeech textToSpeech, int position, ChatListAdapter adapter) {
		if (mStatus == SpeechStatus.SPEAK) {
			int itemCount = adapter.getItemCount();
			for (int i = position; i < itemCount; i++) {
				textToSpeech.speak(regexMessage(adapter.getChatList().get(i).getAuthorDetails().getDisplayName()), TextToSpeech.QUEUE_ADD, null, "speech_id_name" + i);
				textToSpeech.playSilentUtterance(5, TextToSpeech.QUEUE_ADD, "speech_id_silent_short" + i);
				textToSpeech.speak(regexMessage(adapter.getChatList().get(i).getSnippet().getTextMessageDetails().getMessageText()), TextToSpeech.QUEUE_ADD, null, "speech_id_message" + i);
				textToSpeech.playSilentUtterance(50, TextToSpeech.QUEUE_ADD, "speech_id_silent" + i);
				ChatListPresenter.mLastPlayPosition = i;
			}
		}
	}

	public static void stopSpeech(TextToSpeech textToSpeech) {
		textToSpeech.stop();
	}

	private static String regexMessage(String text) {
		return text.replaceAll("(?u)[^(\\w)|(\\x7F-\\xFF)|(\\s)]", "");
	}

	public enum SpeechStatus {
		SPEAK,
		NOT_SPEAK
	}
}

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
				textToSpeech.playSilentUtterance(7000, TextToSpeech.QUEUE_ADD, "speech_id_silent" + i);
				ChatListPresenter.mLastPlayPosition = i;
			}
		}
	}

	public static void stopSpeech(TextToSpeech textToSpeech) {
		textToSpeech.stop();
	}

	private static String regexMessage(String text) {
		String s = text.replaceAll("(?u)[^(а-яА-Яa-zA-Z0-9,.)|\\s]|[\\x21-\\x2B\\x2F]", "");
		return s.replaceAll("([а-яa-zА-ЯA-Z])(\\1+)","$1");
	}

	public enum SpeechStatus {
		SPEAK,
		NOT_SPEAK
	}
}

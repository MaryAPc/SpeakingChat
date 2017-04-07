package com.maryapc.speakingchat.utils;

import android.speech.tts.TextToSpeech;

import com.maryapc.speakingchat.adapter.recycler.ChatListAdapter;
import com.maryapc.speakingchat.presenter.ChatListPresenter;

public class SpeakManager {

	public static SpeechStatus mStatus;
	public static long mInterval;
	public static long mSmallInterval;

	public synchronized static void speechMessages(TextToSpeech textToSpeech, int position, ChatListAdapter adapter) {
		String message;
		if (mStatus == SpeechStatus.SPEAK) {
			int itemCount = adapter.getItemCount();
			for (int i = position; i < itemCount; i++) {
				textToSpeech.speak(regexMessage(adapter.getChatList().get(i).getAuthorDetails().getDisplayName()), TextToSpeech.QUEUE_ADD, null, "speech_id_name" + i);
				textToSpeech.playSilentUtterance(5, TextToSpeech.QUEUE_ADD, "speech_id_silent_short" + i);
				try {
					message = adapter.getChatList().get(i).getSnippet().getTextMessageDetails().getMessageText();
				} catch (NullPointerException e) {
					message = regexDonatMessage("Донат. " + adapter.getChatList().get(i).getSnippet().getSuperChatDetails().getAmountDisplayString()
					          + ". " + adapter.getChatList().get(i).getSnippet().getSuperChatDetails().getUserComment());
				}
				textToSpeech.speak(regexMessage(message), TextToSpeech.QUEUE_ADD, null, String.valueOf(i));
				if (message.length() <= 15) {
					textToSpeech.playSilentUtterance(mSmallInterval * 1000, TextToSpeech.QUEUE_ADD, "speech_id_silent" + i);
				} else {
					textToSpeech.playSilentUtterance(mInterval * 1000, TextToSpeech.QUEUE_ADD, "speech_id_silent" + i);
				}
				ChatListPresenter.mLastPlayPosition = i;
			}
		}
	}

	public static void stopSpeech(TextToSpeech textToSpeech) {
		textToSpeech.stop();
	}

	private static String regexMessage(String text) {
		String s = text.replaceAll("(?u)[^(а-яА-Яa-zA-ZёЁ0-9,.?)|\\s]|[\\x21-\\x2B\\x2F]", "");
		return s.replaceAll("([а-яa-zА-ЯA-ZёЁ])(\\1+)","$1");
	}

	private static String regexDonatMessage(String text) {
		return text.replaceAll("(\\.00)","");
	}

	public enum SpeechStatus {
		SPEAK,
		NOT_SPEAK
	}
}

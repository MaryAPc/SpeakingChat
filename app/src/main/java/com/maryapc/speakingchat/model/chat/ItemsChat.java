package com.maryapc.speakingchat.model.chat;

import com.google.gson.annotations.SerializedName;

public class ItemsChat {

	@SerializedName("kind")
	private String mKind;
	@SerializedName("snippet")
	private Snippet mSnippet;
	@SerializedName("authorDetails")
	private AuthorDetails mAuthorDetails;

	public String getKind() {
		return mKind;
	}

	public Snippet getSnippet() {
		return mSnippet;
	}

	public AuthorDetails getAuthorDetails() {
		return mAuthorDetails;
	}

	public static class TextMessageDetails {
		@SerializedName("messageText")
		private String mMessageText;

		public String getMessageText() {
			return mMessageText;
		}
	}

	public static class Snippet {
		@SerializedName("textMessageDetails")
		private TextMessageDetails mTextMessageDetails;

		public TextMessageDetails getTextMessageDetails() {
			return mTextMessageDetails;
		}
	}

	public static class AuthorDetails {
		@SerializedName("displayName")
		private String mDisplayName;

		public String getDisplayName() {
			return mDisplayName;
		}
	}
}

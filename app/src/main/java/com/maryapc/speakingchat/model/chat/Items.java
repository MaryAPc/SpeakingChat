package com.maryapc.speakingchat.model.chat;

import com.google.gson.annotations.SerializedName;

public class Items {

	@SerializedName("kind")
	private String kind;
	@SerializedName("snippet")
	private Snippet snippet;
	@SerializedName("authorDetails")
	private AuthorDetails authorDetails;

	public String getKind() {
		return kind;
	}

	public Snippet getSnippet() {
		return snippet;
	}

	public AuthorDetails getAuthorDetails() {
		return authorDetails;
	}

	public static class TextMessageDetails {
		@SerializedName("messageText")
		private String messageText;

		public String getMessageText() {
			return messageText;
		}
	}

	public static class Snippet {
		@SerializedName("textMessageDetails")
		private TextMessageDetails textMessageDetails;

		public TextMessageDetails getTextMessageDetails() {
			return textMessageDetails;
		}
	}

	public static class AuthorDetails {
		@SerializedName("displayName")
		private String displayName;

		public String getDisplayName() {
			return displayName;
		}
	}
}

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
		@SerializedName("messageText") //сообщение
		private String mMessageText;

		public String getMessageText() {
			return mMessageText;
		}
	}

	public static class Snippet {
		@SerializedName("textMessageDetails")
		private TextMessageDetails mTextMessageDetails;
		@SerializedName("superChatDetails")
		private SuperChatDetails mSuperChatDetails;

		public TextMessageDetails getTextMessageDetails() {
			return mTextMessageDetails;
		}

		public SuperChatDetails getSuperChatDetails() {
			return mSuperChatDetails;
		}
	}

	public static class AuthorDetails {
		@SerializedName("displayName") //Username
		private String mDisplayName;
		@SerializedName("profileImageUrl")
		private String mProfileImage;

		public String getProfileImage() {
			return mProfileImage;
		}

		public String getDisplayName() {
			return mDisplayName;
		}
	}

	public static class SuperChatDetails {
		@SerializedName("amountDisplayString")
		private String mAmountDisplayString;
		@SerializedName("userComment")
		private String mUserComment;

		public String getAmountDisplayString() {
			return mAmountDisplayString;
		}

		public String getUserComment() {
			return mUserComment;
		}
	}
}

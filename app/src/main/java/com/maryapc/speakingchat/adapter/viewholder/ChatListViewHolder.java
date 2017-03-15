package com.maryapc.speakingchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.maryapc.speakingchat.MyApplication;
import com.maryapc.speakingchat.R;
import com.maryapc.speakingchat.adapter.CircleTransform;
import com.maryapc.speakingchat.model.chat.ItemsChat;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListViewHolder extends RecyclerView.ViewHolder{

	@BindView(R.id.item_message_text_view_username)
	TextView mUsernameTextView;

	@BindView(R.id.item_message_text_view_text_message)
	TextView mMessageTextView;

	@BindView(R.id.item_message_image_view_avatar)
	ImageView mAvatarImageView;

	public ChatListViewHolder(View itemView) {
		super(itemView);
		try {
			ButterKnife.bind(this, itemView);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void bind(ItemsChat model) {
		Picasso.with(MyApplication.getInstance())
				.load(model.getAuthorDetails().getProfileImage())
				.transform(new CircleTransform())
				.into(mAvatarImageView);
		mMessageTextView.setText(model.getSnippet().getTextMessageDetails().getMessageText());
		mUsernameTextView.setText(model.getAuthorDetails().getDisplayName());
	}
}

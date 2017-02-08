package com.maryapc.speakingchat.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.maryapc.speakingchat.R;
import com.maryapc.speakingchat.model.chat.ItemsChat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListViewHolder extends RecyclerView.ViewHolder{

	@BindView(R.id.item_message_text_view_username)
	TextView mUsernameTextView;

	@BindView(R.id.item_message_text_view_text_message)
	TextView mMessageTextView;

	public ChatListViewHolder(View itemView) {
		super(itemView);
		ButterKnife.bind(this, itemView);
	}

	public void bind(ItemsChat model) {
		mMessageTextView.setText(model.getSnippet().getTextMessageDetails().getMessageText());
		mUsernameTextView.setText(model.getAuthorDetails().getDisplayName());
	}
}

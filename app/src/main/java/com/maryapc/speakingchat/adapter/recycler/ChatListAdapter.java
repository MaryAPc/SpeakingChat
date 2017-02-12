package com.maryapc.speakingchat.adapter.recycler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maryapc.speakingchat.R;
import com.maryapc.speakingchat.adapter.viewholder.ChatListViewHolder;
import com.maryapc.speakingchat.model.chat.ItemsChat;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListViewHolder>{

	private ChatListViewHolder mChatListViewHolder;
	private List<ItemsChat> mChatList = new ArrayList<>();
	private OnItemClickListener mOnItemClickListener;

	public interface OnItemClickListener {
		void onItemClick(View view, int position);
	}

	public ChatListAdapter(List<ItemsChat> chatList, OnItemClickListener clickListener) {
		mChatList = chatList;
		mOnItemClickListener = clickListener;
	}

	@Override
	public ChatListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
		mChatListViewHolder = new ChatListViewHolder(view);
		return mChatListViewHolder;
	}

	@Override
	public void onBindViewHolder(ChatListViewHolder holder, int position) {
		holder.bind(mChatList.get(position));
		holder.itemView.setOnClickListener(view -> mOnItemClickListener.onItemClick(view, position));
	}

	@Override
	public int getItemCount() {
		return mChatList.size();
	}

	public void setCollection(@Nullable List<ItemsChat> chatList) {
		if (chatList == null) {
			mChatList = Collections.emptyList();
		} else {
			mChatList = chatList;
		}
		notifyDataSetChanged();
	}

	public void addItems(List<ItemsChat> items) {
		int positionStart = mChatList.size() + 1;
		mChatList.addAll(items);
		notifyItemRangeInserted(positionStart, items.size());
	}

	public List<ItemsChat> getChatList() {
		return mChatList;
	}
}

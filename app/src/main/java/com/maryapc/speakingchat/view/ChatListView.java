package com.maryapc.speakingchat.view;

import java.util.List;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.maryapc.speakingchat.model.chat.ItemsChat;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ChatListView extends MvpView{

	void showEmptyBroadcast();

	void setVisibleSignIn(boolean isSignIn);

	void saveTokens(String refreshToken, String accessToken, String tokenType);

	void saveAccessToken(String accessToken);

	void setChatMessages(List<ItemsChat> itemsChat);

	void showConnectInfo(String titleBroadcast);

	void addMessages(List<ItemsChat> items);

	void showSpeechBar();

	void enableButton(int idButton);

	void switchOfButton(int idButton);

	void startLifeBroadcast();

	void startLifeChat(String lifeChatId);

	void startGettingMessages(String nextPageToken);

	void createIntent(Class<?> activity);

	void showProgressBar(boolean visible);

	void showErrorDialog(int idTitle, int idMessage, boolean clickListener);

	void showHintView();

	void showTtsDialog();

	void goToMarket(String data);
}

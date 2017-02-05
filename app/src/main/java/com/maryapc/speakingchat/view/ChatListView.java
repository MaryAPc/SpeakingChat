package com.maryapc.speakingchat.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ChatListView extends MvpView{

	void showEmptyBroadcast();

	void setVisibleSignIn(boolean isSignIn);

	void saveTokens(String refreshToken, String accessToken, String tokenType);

	void saveAccessToken(String accessToken);

	void showError();
}

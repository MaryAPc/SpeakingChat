package com.maryapc.speakingchat.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.google.android.gms.common.api.GoogleApiClient;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface ChatListView extends MvpView{

	void showAuth(GoogleApiClient apiClient);

	void showSignInfo();
}

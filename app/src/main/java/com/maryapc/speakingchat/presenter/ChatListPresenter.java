package com.maryapc.speakingchat.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.maryapc.speakingchat.view.ChatListView;

@InjectViewState
public class ChatListPresenter extends MvpPresenter<ChatListView> {

	public void signIn(GoogleApiClient apiClient) {
		getViewState().showAuth(apiClient);
	}

	public void signOut(GoogleApiClient apiClient) {
		Auth.GoogleSignInApi.signOut(apiClient).setResultCallback(
				status -> getViewState().showSignInfo());
	}
}

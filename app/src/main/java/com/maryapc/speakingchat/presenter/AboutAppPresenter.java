package com.maryapc.speakingchat.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.maryapc.speakingchat.view.AboutAppView;

@InjectViewState
public class AboutAppPresenter extends MvpPresenter<AboutAppView> {

	public void openUrl(String url) {
		getViewState().showActivity(url);
	}
}

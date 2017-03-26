package com.maryapc.speakingchat;

import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arellomobile.mvp.MvpFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.maryapc.speakingchat.presenter.AboutAppPresenter;
import com.maryapc.speakingchat.view.AboutAppView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutAppFragment extends MvpFragment implements AboutAppView, View.OnClickListener {

	@InjectPresenter
	AboutAppPresenter mPresenter;

	@BindView(R.id.activity_about_app_vk)
	ImageView mVkImageView;

	@BindView(R.id.activity_about_app_youtube)
	ImageView mYoutubeImageView;

	private static final String VK_APP_PACKAGE = "com.vkontakte.android";
	private static final String YOUTUBE_APP_PACKAGE = "com.facebook.katana";

	public static AboutAppFragment newInstance() {
		return new AboutAppFragment();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_about_app, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);
		mVkImageView.setOnClickListener(this);
		mYoutubeImageView.setOnClickListener(this);
		if (Locale.getDefault().getLanguage().equals("en")) {
			mVkImageView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.activity_about_app_vk:
				mPresenter.openUrl(getString(R.string.url_vk_trioka));
				break;
			case R.id.activity_about_app_youtube:
				mPresenter.openUrl(getString(R.string.url_youtube_trioka));
				break;
		}
	}

	@Override
	public void showActivity(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		List<ResolveInfo> resInfo = MyApplication.getInstance().getPackageManager().queryIntentActivities(intent, 0);
		if (resInfo.isEmpty()) {
			return;
		}
		for (ResolveInfo info : resInfo) {
			if (info.activityInfo == null) {
				continue;
			}
			if (VK_APP_PACKAGE.equals(info.activityInfo.packageName) || YOUTUBE_APP_PACKAGE.equals(info.activityInfo.packageName)) {
				intent.setPackage(info.activityInfo.packageName);
				break;
			}
		}
		startActivity(intent);
	}
}

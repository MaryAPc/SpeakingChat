package com.maryapc.speakingchat;

import java.util.List;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.maryapc.speakingchat.presenter.AboutAppPresenter;
import com.maryapc.speakingchat.view.AboutAppView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutAppActivity extends MvpAppCompatActivity implements AboutAppView, View.OnClickListener {

	@InjectPresenter
	AboutAppPresenter mPresenter;

	@BindView(R.id.activity_about_app_button_feedback)
	Button mFeedbackButton;

	@BindView(R.id.activity_about_app_vk)
	ImageView mVkImageView;

	@BindView(R.id.activity_about_app_youtube)
	ImageView mYoutubeImageView;

	private static final String VK_APP_PACKAGE = "com.vkontakte.android";
	private static final String YOUTUBE_APP_PACKAGE = "com.facebook.katana";
	private static final String GOOGLE_PLAY_PACKAGE = "com.android.vending";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_app);
		ButterKnife.bind(this);

		mFeedbackButton.setOnClickListener(this);
		mVkImageView.setOnClickListener(this);
		mYoutubeImageView.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.activity_about_app_button_feedback:
				mPresenter.openUrl(getString(R.string.uri_speaking_chat));
				break;
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
		List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(intent, 0);
		if (resInfo.isEmpty()) {
			return;
		}
		for (ResolveInfo info : resInfo) {
			if (info.activityInfo == null) {
				continue;
			}
			if (VK_APP_PACKAGE.equals(info.activityInfo.packageName) || GOOGLE_PLAY_PACKAGE.equals(info.activityInfo.packageName)
			    || YOUTUBE_APP_PACKAGE.equals(info.activityInfo.packageName)) {
				intent.setPackage(info.activityInfo.packageName);
				break;
			}
		}
		startActivity(intent);
	}
}

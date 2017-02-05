package com.maryapc.speakingchat;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.google.android.gms.auth.api.Auth;
import com.maryapc.speakingchat.adapter.recycler.ChatListAdapter;
import com.maryapc.speakingchat.network.GoogleApiUrls;
import com.maryapc.speakingchat.presenter.ChatListPresenter;
import com.maryapc.speakingchat.view.ChatListView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListActivity extends MvpAppCompatActivity implements View.OnClickListener, ChatListView, ChatListAdapter.OnItemClickListener {

	@InjectPresenter
	ChatListPresenter mPresenter;
	@BindView(R.id.activity_chat_list_button_connect_broadcast)
	Button mConnectBroadcastButton;

	@BindView(R.id.activity_chat_list_recycler_list)
	RecyclerView mChatListRecyclerView;

	@BindView(R.id.activity_chat_list_web_view)
	WebView mWebView;

	private static final String TAG = "ChatListActivity";

	private static final String APP_PREFERENCES = "app_preferences";
	private static final String SIGN_IN = "sign_in";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String TOKEN_TYPE = "token_type";

	private SharedPreferences mSharedPreferences;
	private ChatListAdapter mChatListAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_list);
		ButterKnife.bind(this);

		mConnectBroadcastButton.setOnClickListener(this);
		mChatListAdapter = new ChatListAdapter(new ArrayList<>(), this);
		mChatListRecyclerView.setAdapter(mChatListAdapter);

		mSharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		ChatListPresenter.mRefreshToken = mSharedPreferences.getString(REFRESH_TOKEN, "");
		ChatListPresenter.mAccessToken = mSharedPreferences.getString(ACCESS_TOKEN, "");
		ChatListPresenter.mTokenType = mSharedPreferences.getString(TOKEN_TYPE, "");

		if (mSharedPreferences.getBoolean(SIGN_IN, false)) { //вход выполнен
			mPresenter.visibleSignIn(true);
			mPresenter.checkToken();
		} else { //выполняем вход
			mPresenter.visibleSignIn(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.activity_chat_list_button_connect_broadcast:
				mPresenter.getLifeBroadcast();
				break;
		}
	}

	@Override
	public void showEmptyBroadcast() {
		Toast.makeText(this, R.string.empty_list_broadcast, Toast.LENGTH_LONG).show();
	}

	@Override
	public void setVisibleSignIn(boolean isSignIn) {
		if (isSignIn) {
			mWebView.setVisibility(View.GONE);
		} else {
			mChatListRecyclerView.setVisibility(View.GONE);
			mConnectBroadcastButton.setVisibility(View.GONE);
			mWebView.setVisibility(View.VISIBLE);
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			mWebView.loadUrl(GoogleApiUrls.getSignInUrl());
			mWebView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					String cod = view.getTitle();
					if (cod.split("=")[0].equals("Success code")) {
						Intent temp = new Intent(view.getContext(), Auth.class);
						mWebView.setVisibility(View.INVISIBLE);
						String authCode = cod.split("=")[1];
						setResult(RESULT_OK, temp);
						mSharedPreferences.edit().putBoolean(SIGN_IN, true).apply();
						mPresenter.getAccessToken(authCode);
						mConnectBroadcastButton.setVisibility(View.VISIBLE);
						mChatListRecyclerView.setVisibility(View.VISIBLE);
						mWebView.setVisibility(View.GONE);
					} else {
						mSharedPreferences.edit().putBoolean(SIGN_IN, false).apply();
					}
				}
			});
		}
	}

	@Override
	public void saveTokens(String refreshToken, String accessToken, String tokenType) {
		mSharedPreferences.edit()
				.putString(REFRESH_TOKEN, refreshToken)
				.putString(ACCESS_TOKEN, accessToken)
				.putString(TOKEN_TYPE, tokenType)
				.apply();
	}

	@Override
	public void saveAccessToken(String accessToken) {
		mSharedPreferences.edit()
				.putString(ACCESS_TOKEN, accessToken)
				.apply();
	}

	@Override
	public void showError() {
		Toast.makeText(this, R.string.error_connect, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onItemClick(View view, int position) {

	}
}
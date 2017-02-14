package com.maryapc.speakingchat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.maryapc.speakingchat.adapter.recycler.ChatListAdapter;
import com.maryapc.speakingchat.model.chat.ItemsChat;
import com.maryapc.speakingchat.network.GoogleApiUrls;
import com.maryapc.speakingchat.presenter.ChatListPresenter;
import com.maryapc.speakingchat.view.ChatListView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListActivity extends MvpAppCompatActivity implements View.OnClickListener,
                                                                      ChatListView,
                                                                      ChatListAdapter.OnItemClickListener,
                                                                      TextToSpeech.OnInitListener,
                                                                      GoogleApiClient.OnConnectionFailedListener {
	@InjectPresenter
	ChatListPresenter mPresenter;

	@BindView(R.id.activity_chat_list_button_connect_broadcast)
	Button mConnectBroadcastButton;

	@BindView(R.id.activity_chat_list_recycler_list)
	RecyclerView mChatListRecyclerView;

	@BindView(R.id.activity_chat_list_web_view)
	WebView mWebView;

	@BindView(R.id.activity_chat_list_linear_layout_play_bar)
	LinearLayout mSpeechBar;

	@BindView(R.id.activity_chat_list_button_play)
	Button mPlayButton;

	@BindView(R.id.activity_chat_list_button_stop)
	Button mStopButton;

	private static boolean LAST_MESSAGE_DONE = false;
	private static final String TAG = "ChatListActivity";
	private static final String APP_PREFERENCES = "app_preferences";
	private static final String SIGN_IN = "sign_in";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String TOKEN_TYPE = "token_type";

	private SharedPreferences mSharedPreferences;
	private ChatListAdapter mChatListAdapter;
	private LinearLayoutManager mLayoutManager;
	private TextToSpeech mTextToSpeech;

	private GoogleApiClient mGoogleApiClient;

	private boolean isStopScroll;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_list);
		ButterKnife.bind(this);

		mConnectBroadcastButton.setOnClickListener(this);
		mPlayButton.setOnClickListener(this);
		mStopButton.setOnClickListener(this);

		mChatListAdapter = new ChatListAdapter(new ArrayList<>(), this);
		mLayoutManager = new LinearLayoutManager(this);
		mTextToSpeech = new TextToSpeech(this, this);
		mChatListRecyclerView.setLayoutManager(mLayoutManager);
		mChatListRecyclerView.setAdapter(mChatListAdapter);
		mSharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		ChatListPresenter.mRefreshToken = mSharedPreferences.getString(REFRESH_TOKEN, "");
		ChatListPresenter.mAccessToken = mSharedPreferences.getString(ACCESS_TOKEN, "");
		ChatListPresenter.mTokenType = mSharedPreferences.getString(TOKEN_TYPE, "");

		SpeakService.mStatus = SpeakService.SpeechStatus.NOT_SPEAK;

		prepareGoogleClient();

		if (mSharedPreferences.getBoolean(SIGN_IN, false)) { //вход выполнен
			mPresenter.visibleSignIn(true);
			mPresenter.checkToken();
		} else { //выполняем вход
			mPresenter.visibleSignIn(false);
		}

		mChatListRecyclerView.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (dy < 0) {
					isStopScroll = true;
				} else if (dy > 30) {
					isStopScroll = false;
				}
			}
		});

		mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
			@Override
			public void onStart(String s) {
			}

			@Override
			public void onDone(String s) {
				LAST_MESSAGE_DONE = s.equals("speech_id_silent" + ChatListPresenter.mLastPlayPosition);
			}

			@Override
			public void onError(String s) {
			}
		});
	}

	private void prepareGoogleClient() {
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestEmail()
				.build();
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, this)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();
	}

	private void signOut() {
		Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(status -> revokeAccess());
	}

	private void revokeAccess() {
		Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(status -> {
			mSharedPreferences.edit().putBoolean(SIGN_IN, false).apply();
			mPresenter.unsubscribeAll();
			mChatListAdapter.clean();
			mPresenter.visibleSignIn(false);
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.main_menu_sign_out:
				mPresenter.stopSpeech(mTextToSpeech);
				mSpeechBar.setVisibility(View.GONE);
				signOut();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDestroy() {
		if (mTextToSpeech != null) {
			mTextToSpeech.stop();
			mTextToSpeech.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.activity_chat_list_button_connect_broadcast:
				mPresenter.stopSpeech(mTextToSpeech);
				mPresenter.unsubscribeAll();
				mChatListAdapter.clean();
				mPresenter.checkToken();
				break;
			case R.id.activity_chat_list_button_stop:
				mPresenter.stopSpeech(mTextToSpeech);
				break;
			case R.id.activity_chat_list_button_play:
				SpeakService.mStatus = SpeakService.SpeechStatus.SPEAK;
				mPresenter.speech(mTextToSpeech, ChatListPresenter.mLastPlayPosition, mChatListAdapter);
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
						mWebView.setVisibility(View.INVISIBLE);
						Intent temp = new Intent(view.getContext(), Auth.class);
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
	public void setChatMessages(List<ItemsChat> itemsChat) {
		mChatListAdapter.setCollection(itemsChat);
	}

	@Override
	public void showConnectInfo(String titleBroadcast) {
		Toast.makeText(this, getString(R.string.connect_to_broadcast_info) + titleBroadcast, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void addMessages(List<ItemsChat> items) {
		ChatListPresenter.mSubscriptionNewMessages.unsubscribe();
		mChatListAdapter.addItems(items);
		if (!isStopScroll) {
			mLayoutManager.scrollToPosition(mChatListAdapter.getItemCount() - 1);
		}
		if (items.size() != 0 && LAST_MESSAGE_DONE) {
			mPresenter.speech(mTextToSpeech, ChatListPresenter.mLastPlayPosition + 1, mChatListAdapter);
		}
		mPresenter.getNextChatMessages();
	}

	@Override
	public void showSpeechBar() {
		mSpeechBar.setVisibility(View.VISIBLE);
		mStopButton.setEnabled(true);
		mPlayButton.setEnabled(true);
	}

	@Override
	public void enableButton(int idButton) {
		switch (idButton) {
			case R.id.activity_chat_list_button_play:
				mPlayButton.setEnabled(true);
				break;
			case R.id.activity_chat_list_button_stop:
				mStopButton.setEnabled(true);
				break;
		}
	}

	@Override
	public void switchOfButton(int idButton) {
		switch (idButton) {
			case R.id.activity_chat_list_button_play:
				mPlayButton.setEnabled(false);
				break;
			case R.id.activity_chat_list_button_stop:
				mStopButton.setEnabled(false);
				break;
		}
	}

	@Override
	public void startLifeBroadcast() {
		mPresenter.getLifeBroadcast();
	}

	@Override
	public void startLifeChat(String lifeChatId) {
		mPresenter.getLifeChat(lifeChatId);
	}

	@Override
	public void startGettingMessages(String nextPageToken) {
		mPresenter.getNextChatMessages();
	}

	@Override
	public void onItemClick(View view, int position) {
		SpeakService.mStatus = SpeakService.SpeechStatus.SPEAK;
		mPresenter.speech(mTextToSpeech, position, mChatListAdapter);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			Locale locale = new Locale("ru");
			int result = mTextToSpeech.setLanguage(locale);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "Язык не поддерживается");
			}
		} else {
			Log.e("TTS", "Ошибка");
			Toast.makeText(this, "Ошибка голосового воспроизведения", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}
}
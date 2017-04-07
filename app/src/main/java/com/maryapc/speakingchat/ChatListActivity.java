package com.maryapc.speakingchat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.maryapc.speakingchat.adapter.recycler.ChatListAdapter;
import com.maryapc.speakingchat.databinding.ActivityDrawerBinding;
import com.maryapc.speakingchat.databinding.NavigationHeaderBinding;
import com.maryapc.speakingchat.model.User;
import com.maryapc.speakingchat.model.chat.ItemsChat;
import com.maryapc.speakingchat.presenter.ChatListPresenter;
import com.maryapc.speakingchat.utils.AppPreferences;
import com.maryapc.speakingchat.utils.SpeakManager;
import com.maryapc.speakingchat.view.ChatListView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatListActivity extends MvpAppCompatActivity implements View.OnClickListener,
                                                                      ChatListView,
                                                                      ChatListAdapter.OnItemClickListener,
                                                                      TextToSpeech.OnInitListener,
                                                                      GoogleApiClient.OnConnectionFailedListener,
                                                                      NavigationView.OnNavigationItemSelectedListener {
	@InjectPresenter
	ChatListPresenter mPresenter;

	@Inject
	AppPreferences mAppPreferences;

	@BindView(R.id.activity_chat_list_recycler_list)
	RecyclerView mChatListRecyclerView;

	@BindView(R.id.activity_chat_list_linear_layout_play_bar)
	LinearLayout mSpeechBar;

	@BindView(R.id.activity_chat_list_button_play)
	ImageButton mPlayButton;

	@BindView(R.id.activity_chat_list_button_stop)
	ImageButton mStopButton;

	@BindView(R.id.activity_chat_list_progress_bar)
	ProgressBar mProgressBar;

	@BindView(R.id.activity_chat_list_relative_layout_hint)
	RelativeLayout mHintRelativeLayout;

	@BindView(R.id.activity_chat_list_button_hint_ok)
	Button mOkHintButton;

	@BindView(R.id.activity_chat_list_text_view_empty_broadcast)
	TextView mEmptyBroadcastTextView;

	@BindView(R.id.activity_chat_list_button_sign_in)
	SignInButton mSignInButton;

	@BindView(R.id.activity_chat_list_linear_layout_sign_in)
	LinearLayout mSignInLinearLayout;

	@BindView(R.id.activity_chat_list_linear_layout_content)
	LinearLayout mContentLinearLayout;

	@BindView(R.id.activity_drawer_layout)
	DrawerLayout mDrawerLayout;

	@BindView(R.id.activity_drawer_navigation_view)
	NavigationView mNavigationView;

	@BindView(R.id.activity_toolbar)
	Toolbar mToolbar;

	@BindView(R.id.activity_app_bar_chat_content)
	View mChatIncludeView;

	private static final int TTS_REQUEST_CODE = 11;
	private static final int RC_GET_AUTH_CODE = 22;
	private static final String TAG = "ChatListActivity";
	private static final String GOOGLE_PLAY_PACKAGE = "com.android.vending";
	private static final String PREFERENCES_SILENT = "silent_interval";
	private static final String PREFERENCES_SILENT_SMALL = "silent_interval_small";
	private static final String PREFERENCES_SCREEN_ON = "screen_on";

	private static boolean isStopScroll;
	private static boolean sLastMessageDone = false;
	private SharedPreferences mDefaultPreferences;
	private ChatListAdapter mChatListAdapter;
	private LinearLayoutManager mLayoutManager;
	private GoogleApiClient mGoogleApiClient;
	private NavigationHeaderBinding mHeaderBinding;
	private static TextToSpeech mTextToSpeech;
	private AboutAppFragment mAboutAppFragment = AboutAppFragment.newInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActivityDrawerBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_drawer);
		ButterKnife.bind(this);
		mHeaderBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.navigation_header, binding.activityDrawerNavigationView, false);
		binding.activityDrawerNavigationView.addHeaderView(mHeaderBinding.getRoot());

		setSupportActionBar(mToolbar);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mDrawerLayout.setDrawerListener(toggle);
		toggle.syncState();

		MyApplication.getAppComponent().inject(this);

		mPlayButton.setOnClickListener(this);
		mStopButton.setOnClickListener(this);
		mSignInButton.setOnClickListener(this);
		mNavigationView.setNavigationItemSelectedListener(this);

		mChatListAdapter = new ChatListAdapter(new ArrayList<>(), this);
		mLayoutManager = new LinearLayoutManager(this);
		mTextToSpeech = new TextToSpeech(this, this);

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, mLayoutManager.getOrientation());
		mChatListRecyclerView.addItemDecoration(dividerItemDecoration);
		mChatListRecyclerView.setLayoutManager(mLayoutManager);
		mChatListRecyclerView.setAdapter(mChatListAdapter);

		mDefaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (!mAppPreferences.isUpdateSignIn()) {
			mAppPreferences.setUpdateSignIn(true);
		}
		ChatListPresenter.mRefreshToken = mAppPreferences.getRefreshToken();
		ChatListPresenter.mAccessToken = mAppPreferences.getAccessToken();
		ChatListPresenter.mTokenType = mAppPreferences.getTokenType();

		SpeakManager.mStatus = SpeakManager.SpeechStatus.NOT_SPEAK;

		prepareGoogleClient();

		if (mAppPreferences.isSignIn()) { //вход выполнен
			mPresenter.visibleSignIn(true);
			mPresenter.checkToken();
			mPresenter.setProfileData();
		} else { //выполняем вход
			mPresenter.visibleSignIn(false);
		}

		mChatListRecyclerView.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				if (dy < 0) {
					isStopScroll = true;
				} else if (dy > 70) {
					isStopScroll = false;
				}
			}
		});
		mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
			@Override
			public void onStart(String s) {
				if (!s.startsWith("s")) {
					ChatListPresenter.mSpeakMessage = Integer.valueOf(s);
				}
			}

			@Override
			public void onDone(String s) {
				sLastMessageDone = s.equals("speech_id_silent" + ChatListPresenter.mLastPlayPosition);
			}

			@Override
			public void onError(String s) {
			}
		});
	}

	private void prepareGoogleClient() {
		String serverClientId = getString(R.string.server_client_id);
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestScopes(new Scope("https://www.googleapis.com/auth/youtube.readonly"))
				.requestServerAuthCode(serverClientId)
				.requestEmail()
				.build();

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, this)
				.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
				.build();
	}

	private void resetConnectBroadcast() {
		mPresenter.unsubscribeAll();
		mPresenter.stopSpeech(mTextToSpeech);
		mChatListAdapter.clean();
		mEmptyBroadcastTextView.setVisibility(View.GONE);
	}

	@Override
	public void signOut() {
		Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
				status -> {
					Auth.GoogleSignInApi.signOut(mGoogleApiClient);
					mAppPreferences.setSignIn(false);
					mPresenter.visibleSignIn(false);
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TTS_REQUEST_CODE) {
			mTextToSpeech = new TextToSpeech(this, this);
		} else if (requestCode == RC_GET_AUTH_CODE) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess()) {
				GoogleSignInAccount acct = result.getSignInAccount();
				assert acct != null;
				mPresenter.saveUserData(String.valueOf(acct.getPhotoUrl()), acct.getDisplayName(), acct.getEmail());
				String authCode = acct.getServerAuthCode();
				if (BuildConfig.DEBUG) {
					Log.d(TAG, authCode);
				}
				mAppPreferences.setSignIn(true);
				mPresenter.getAccessToken(authCode);
				mSignInLinearLayout.setVisibility(View.GONE);
				mContentLinearLayout.setVisibility(View.VISIBLE);
				mPresenter.setProfileData();
			} else {
				mAppPreferences.setSignIn(false);
				mPresenter.errorDialog(R.string.error, R.string.error_auth, false);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.main_menu_about:
				mChatIncludeView.setVisibility(View.GONE);
				mPresenter.insertFragment(mAboutAppFragment);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()) {
			case R.id.drawer_menu_connect_broadcast:
				mChatIncludeView.setVisibility(View.VISIBLE);
				mDrawerLayout.closeDrawers();
				resetConnectBroadcast();
				mPresenter.checkToken();
				return true;
			case R.id.drawer_menu_settings:
				mPresenter.startActivity(ChatPreferences.class);
				mDrawerLayout.closeDrawers();
				mPresenter.stopSpeech(mTextToSpeech);
				return true;
			case R.id.drawer_menu_feedback:
				mDrawerLayout.closeDrawers();
				mPresenter.goGooglePlay(getString(R.string.uri_speaking_chat), false, 0);
				return true;
			case R.id.drawer_menu_sign_out:
				mDrawerLayout.closeDrawers();
				resetConnectBroadcast();
				mSpeechBar.setVisibility(View.GONE);
				mPresenter.saveUserData("", "", "");
				mPresenter.setProfileData();
				mPresenter.signOut();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		long newInterval = 7;
		long newSmallInterval = 2;
		try {
			newInterval = Long.parseLong(mDefaultPreferences.getString(PREFERENCES_SILENT, "7"));
			newSmallInterval = Long.parseLong(mDefaultPreferences.getString(PREFERENCES_SILENT_SMALL, "2"));
		} catch (NumberFormatException e) {
			mDefaultPreferences.edit().putString(PREFERENCES_SILENT, "7").apply();
			mDefaultPreferences.edit().putString(PREFERENCES_SILENT_SMALL, "2").apply();
			mPresenter.errorDialog(R.string.error, R.string.error_interval, true);
		} finally {
			mPresenter.setNewInterval(newInterval, newSmallInterval);
		}
		if (mDefaultPreferences.getBoolean(PREFERENCES_SCREEN_ON, true)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
	}

	@Override
	public void onBackPressed() {
		if (!mChatIncludeView.isShown()) {
			mChatIncludeView.setVisibility(View.VISIBLE);
		} else {
			super.onBackPressed();
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
			case R.id.activity_chat_list_button_stop:
				mPresenter.stopSpeech(mTextToSpeech);
				break;
			case R.id.activity_chat_list_button_play:
				SpeakManager.mStatus = SpeakManager.SpeechStatus.SPEAK;
				mPresenter.speech(mTextToSpeech, ChatListPresenter.mSpeakMessage, mChatListAdapter);
				break;
			case R.id.activity_chat_list_button_sign_in:
				mPresenter.getAuthCode();
				break;
		}
	}

	@Override
	public void showEmptyBroadcast() {
		mEmptyBroadcastTextView.setVisibility(View.VISIBLE);
	}

	@Override
	public void setVisibleSignIn(boolean isSignIn) {
		if (isSignIn) {
			mSignInLinearLayout.setVisibility(View.GONE);
		} else {
			mContentLinearLayout.setVisibility(View.GONE);
			Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
			mSignInLinearLayout.setVisibility(View.VISIBLE);
			mSignInLinearLayout.startAnimation(animFadeIn);
		}
	}

	@Override
	public void saveTokens(String refreshToken, String accessToken, String tokenType) {
		mAppPreferences.saveRefreshToken(refreshToken);
		mAppPreferences.saveAccessToken(accessToken);
		mAppPreferences.saveTokenType(tokenType);
	}

	@Override
	public void saveAccessToken(String accessToken) {
		mAppPreferences.saveAccessToken(accessToken);
	}

	@Override
	public void setChatMessages(List<ItemsChat> itemsChat) {
		mChatListAdapter.setCollection(itemsChat);
	}

	@Override
	public void showConnectInfo(String titleBroadcast) {
		String connectTo = getString(R.string.connect_to_broadcast_info, titleBroadcast);
		Toast.makeText(this, connectTo, Toast.LENGTH_SHORT).show();
	}

	@Override
	public synchronized void addMessages(List<ItemsChat> items) {
		ChatListPresenter.mSubscriptionNewMessages.unsubscribe();
		mChatListAdapter.addItems(items);
		if (!isStopScroll) {
			mLayoutManager.scrollToPosition(mChatListAdapter.getItemCount() - 1);
		}
		if (items.size() != 0 && sLastMessageDone) {
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
	public void addFragment(android.app.Fragment fragment) {
		getFragmentManager()
				.beginTransaction()
				.replace(R.id.activity_app_bar_frame_layout_container, fragment)
				.commit();
	}

	@Override
	public void showProgressBar(boolean visible) {
		ChatListActivity.this.runOnUiThread(() -> mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE));
	}

	@Override
	public void showErrorDialog(int idTitle, int idMessage, boolean clickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(idTitle)
				.setMessage(idMessage)
				.setPositiveButton(R.string.ok, clickListener ? (DialogInterface.OnClickListener) (dialogInterface, i) ->
						mPresenter.startActivity(ChatPreferences.class) : null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void showHintView() {
		if (mAppPreferences.isFirstLaunch()) {
			Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
			mHintRelativeLayout.setVisibility(View.VISIBLE);
			Animation animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
			mHintRelativeLayout.startAnimation(animFadeIn);
			mOkHintButton.setOnClickListener(view -> {
				mHintRelativeLayout.startAnimation(animFadeOut);
				mHintRelativeLayout.setVisibility(View.GONE);
				mAppPreferences.setFirstLaunch(false);
			});
		}
	}

	@Override
	public void showTtsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.attention)
				.setMessage(R.string.tts_no_install_message)
				.setPositiveButton(R.string.install, (dialogInterface, i) -> mPresenter.goGooglePlay(getString(R.string.uri_tts), true, TTS_REQUEST_CODE))
				.setNegativeButton(R.string.cansel, (dialogInterface, i) ->
						Toast.makeText(MyApplication.getInstance(), R.string.speech_imposible, Toast.LENGTH_LONG).show());
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	public void goToMarket(String data, boolean forResult, int requestCode) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
		List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(intent, 0);
		if (resInfo.isEmpty()) {
			return;
		}
		for (ResolveInfo info : resInfo) {
			if (info.activityInfo == null) {
				continue;
			}
			if (GOOGLE_PLAY_PACKAGE.equals(info.activityInfo.packageName)) {
				intent.setPackage(info.activityInfo.packageName);
				break;
			}
		}
		if (forResult) {
			startActivityForResult(intent, requestCode);
		} else {
			startActivity(intent);
		}
	}

	@Override
	public void showActivity(Class<?> activity) {
		Intent intent = new Intent(ChatListActivity.this, activity);
		startActivity(intent);
	}

	@Override
	public void setProfileData() {
		User user = new User(mAppPreferences.getUserAvatar(), mAppPreferences.getUserName(), mAppPreferences.getUserEmail());
		mHeaderBinding.setUser(user);
	}

	@Override
	public void saveUserData(String photoUrl, String displayName, String email) {
		mAppPreferences.saveUserAvatar(photoUrl);
		mAppPreferences.saveUserName(displayName);
		mAppPreferences.saveUserEmail(email);
	}

	@Override
	public void startSignInActivity() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_GET_AUTH_CODE);
	}

	@Override
	public void onItemClick(View view, int position) {
		SpeakManager.mStatus = SpeakManager.SpeechStatus.SPEAK;
		mPresenter.speech(mTextToSpeech, position, mChatListAdapter);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			Locale locale = new Locale("ru");
			int result = mTextToSpeech.setLanguage(locale);
			if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
				if (BuildConfig.DEBUG) {
					Log.e("TTS", "Язык не поддерживается");
				}
			}
		} else {
			if (BuildConfig.DEBUG) {
				Log.e("TTS", "Ошибка");
			}
			mPresenter.ttsErrorDialog();
		}
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
	}
}
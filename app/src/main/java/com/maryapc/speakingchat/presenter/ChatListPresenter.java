package com.maryapc.speakingchat.presenter;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.maryapc.speakingchat.BuildConfig;
import com.maryapc.speakingchat.MyApplication;
import com.maryapc.speakingchat.R;
import com.maryapc.speakingchat.adapter.recycler.ChatListAdapter;
import com.maryapc.speakingchat.model.TokenResponse;
import com.maryapc.speakingchat.model.brooadcast.BroadcastResponse;
import com.maryapc.speakingchat.model.brooadcast.ItemsBroadcast;
import com.maryapc.speakingchat.model.chat.ChatResponse;
import com.maryapc.speakingchat.network.RetrofitService;
import com.maryapc.speakingchat.utils.SpeakService;
import com.maryapc.speakingchat.view.ChatListView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@InjectViewState
public class ChatListPresenter extends MvpPresenter<ChatListView> {

	private static boolean FIRST_CONNECT = true;
	private static String mNextPageToken = "";
	private Subscription mSubscriptionBroadcast;
	private Subscription mSubscriptionChat;
	public static Subscription mSubscriptionNewMessages;

	private static String mLifeChatId = "";
	public static String mAccessToken = "";
	public static String mRefreshToken = "";
	public static String mTokenType = "";

	public static int mLastPlayPosition = 0;
	public static int mSpeakMessage = 0;
	private int mPollingInterval;

	public void visibleSignIn(boolean isSignIn) {
		getViewState().setVisibleSignIn(isSignIn);
	}

	public void checkToken() {
		Call<TokenResponse> responseCall = RetrofitService.getInstance().createApi().getTokenInfo(mAccessToken);
		responseCall.enqueue(new retrofit2.Callback<TokenResponse>() {
			@Override
			public void onResponse(Call<TokenResponse> call, retrofit2.Response<TokenResponse> response) {
				if (response.isSuccessful()) {
					getViewState().startLifeBroadcast();
					if (BuildConfig.DEBUG) Log.e("tag", "check success");
				} else { //невалидный токен
					getNewAccessToken(true);
					if (BuildConfig.DEBUG) Log.e("tag", "check NOT success");
				}
			}

			@Override
			public void onFailure(Call<TokenResponse> call, Throwable t) {
			}
		});
	}

	private void getNewAccessToken(boolean isNewConnect) {
		OkHttpClient client = new OkHttpClient();
		RequestBody requestBody = new FormEncodingBuilder()
				.add("refresh_token", mRefreshToken)
				.add("client_id", MyApplication.getInstance().getString(R.string.server_client_id))
				.add("client_secret", MyApplication.getInstance().getString(R.string.client_secret))
				.add("grant_type", "refresh_token")
				.build();
		final Request request = new Request.Builder()
				.url("https://www.googleapis.com/oauth2/v4/token")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.post(requestBody)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Request request, IOException e) {
				if (BuildConfig.DEBUG) Log.e("tag", e.toString());
			}

			@Override
			public void onResponse(Response response) throws IOException {
				try {
					JSONObject jsonObject = new JSONObject(response.body().string());
					final String message = jsonObject.toString();
					if (BuildConfig.DEBUG) Log.d("tag", message + "new token");
					mAccessToken = jsonObject.get("access_token").toString();
					mTokenType = jsonObject.get("token_type").toString();
					getViewState().saveAccessToken(mAccessToken);
					if (isNewConnect) {
						FIRST_CONNECT = true;
						getViewState().startLifeBroadcast();
					} else {
						getViewState().startGettingMessages(mNextPageToken);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void getAccessToken(String authCode) {
		OkHttpClient client = new OkHttpClient();
		RequestBody requestBody = new FormEncodingBuilder()
				.add("grant_type", "authorization_code")
				.add("client_id", MyApplication.getInstance().getString(R.string.server_client_id))
				.add("client_secret", MyApplication.getInstance().getString(R.string.client_secret))
				.add("code", authCode)
				.build();
		final Request request = new Request.Builder()
				.url("https://www.googleapis.com/oauth2/v4/token")
				.header("Content-Type", "application/x-www-form-urlencoded")
				.post(requestBody)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(final Request request, final IOException e) {
				Log.e("tag", e.toString());
			}

			@Override
			public void onResponse(Response response) throws IOException {
				try {
					JSONObject jsonObject = new JSONObject(response.body().string());
					final String message = jsonObject.toString();
					if (BuildConfig.DEBUG) Log.d("tag", message);
					mAccessToken = jsonObject.get("access_token").toString();
					mTokenType = jsonObject.get("token_type").toString();
					mRefreshToken = jsonObject.get("refresh_token").toString();
					getViewState().saveTokens(mRefreshToken, mAccessToken, mTokenType);
					FIRST_CONNECT = true;
					getViewState().startLifeBroadcast();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void getLifeBroadcast() {
		getViewState().showProgressBar(true);
		mSubscriptionBroadcast = requestBroadcast()
				.subscribeOn(Schedulers.io())
				.map(BroadcastResponse::getItems)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<List<ItemsBroadcast>>() {
					boolean isEmptyBroadcast;
					String titleBroadcast = "";

					@Override
					public void onCompleted() {
						if (!isEmptyBroadcast) {
							if (FIRST_CONNECT) {
								FIRST_CONNECT = false;
								getViewState().showConnectInfo(titleBroadcast);
							}
							getViewState().showSpeechBar();
							getViewState().startLifeChat(mLifeChatId);
						}
					}

					@Override
					public void onError(Throwable e) {
						if (BuildConfig.DEBUG) Log.d("presenter", "Error getLifeBroadcast");
						if (e instanceof HttpException) {
							HttpException exception = (HttpException) e;
							if (exception.code() == 403) {
								getViewState().showErrorDialog(R.string.error, R.string.check_settings_broadcast, false);
								getViewState().showProgressBar(false);
							}
						}
					}

					@Override
					public void onNext(List<ItemsBroadcast> itemses) {
						if (itemses.size() == 0) {
							getViewState().showProgressBar(false);
							getViewState().showEmptyBroadcast();
							isEmptyBroadcast = true;
						} else {
							isEmptyBroadcast = false;
							mLifeChatId = itemses.get(0).getSnippet().getLiveChatId();
							titleBroadcast = itemses.get(0).getSnippet().getTitle();
						}
					}
				});

	}

	public void getLifeChat(String lifeChatId) {
		mSubscriptionChat = requestChat(lifeChatId)
				.subscribeOn(Schedulers.io())
				.delay(3000, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<ChatResponse>() {
					@Override
					public void onCompleted() {
						getViewState().showProgressBar(false);
						getViewState().showHintView();
						getViewState().startGettingMessages(mNextPageToken);
					}

					@Override
					public void onError(Throwable e) {
						if (BuildConfig.DEBUG) Log.d("presenter", "Error getLifeChat");
					}

					@Override
					public void onNext(ChatResponse chatResponse) {
						mNextPageToken = chatResponse.getNextPageToken();
						mPollingInterval = chatResponse.getPollingIntervalMillis();
						getViewState().setChatMessages(chatResponse.getItems());
					}
				});
	}

	public void getNextChatMessages() {
		mSubscriptionNewMessages = requestNextMessages(mNextPageToken)
				.subscribeOn(Schedulers.io())
				.delay(mPollingInterval, TimeUnit.MILLISECONDS)
				.timeout(10, TimeUnit.SECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<ChatResponse>() {
					@Override
					public void onCompleted() {
					}

					@Override
					public void onError(Throwable e) {
						handleError(e);
					}

					@Override
					public void onNext(ChatResponse chatResponse) {
						mNextPageToken = chatResponse.getNextPageToken();
						getViewState().addMessages(chatResponse.getItems());
					}
				});
	}

	private void handleError(Throwable e) {
		e.printStackTrace();
		if (e instanceof HttpException) {
			HttpException exception = (HttpException) e;
			try {
				JSONObject jsonObject = new JSONObject(exception.response().errorBody().string());
				String message = jsonObject.toString();
				if (BuildConfig.DEBUG) Log.d("presenter HttpException", message);
			} catch (JSONException | IOException e1) {
				e1.printStackTrace();
			}
			switch (((HttpException) e).code()) {
				case 403:
					if (BuildConfig.DEBUG) Log.d("presenter", "Error 403");
					mSubscriptionChat.unsubscribe();
					mSubscriptionNewMessages.unsubscribe();
					getViewState().startLifeChat(mLifeChatId);
					break;
				case 401:
					mSubscriptionNewMessages.unsubscribe();
					getNewAccessToken(false);
					if (BuildConfig.DEBUG) Log.d("presenter", "Error 401");
					break;
				default:
					if (BuildConfig.DEBUG) Log.d("presenter", "unknown Error");
			}
			if (BuildConfig.DEBUG) Log.d("presenter", "Error getNextChatMessages");
		} else if (e instanceof UnknownHostException) {
			mSubscriptionNewMessages.unsubscribe();
			getViewState().startGettingMessages(mNextPageToken);
		} else if (e instanceof SocketTimeoutException) {
			mSubscriptionNewMessages.unsubscribe();
			getViewState().startGettingMessages(mNextPageToken);
		}
	}

	private Observable<BroadcastResponse> requestBroadcast() {
		return RetrofitService.getInstance().createApi().getBroadcast(mTokenType + " " + mAccessToken, "snippet", "active", "all");
	}

	private Observable<ChatResponse> requestChat(String lifeChatId) {
		return RetrofitService.getInstance().createApi().getChat(mTokenType + " " + mAccessToken, lifeChatId, "snippet, authorDetails");
	}

	private Observable<ChatResponse> requestNextMessages(String nextPageToken) {
		return RetrofitService.getInstance().createApi().getNextChatMessage(mTokenType + " " + mAccessToken, mLifeChatId, "snippet, authorDetails", nextPageToken);
	}

	public void speech(TextToSpeech textToSpeech, int position, ChatListAdapter adapter) {
		textToSpeech.stop();
		if (SpeakService.mStatus == SpeakService.SpeechStatus.SPEAK) {
			if (adapter.getItemCount() != 0) {
				getViewState().enableButton(R.id.activity_chat_list_button_stop);
				getViewState().switchOfButton(R.id.activity_chat_list_button_play);
				SpeakService.speechMessages(textToSpeech, position, adapter);
			}
		}
	}

	public void stopSpeech(TextToSpeech textToSpeech) {
		SpeakService.mStatus = SpeakService.SpeechStatus.NOT_SPEAK;
		SpeakService.stopSpeech(textToSpeech);
		getViewState().switchOfButton(R.id.activity_chat_list_button_stop);
		getViewState().enableButton(R.id.activity_chat_list_button_play);
	}

	public void unsubscribeAll() {
		FIRST_CONNECT = true;
		if (mSubscriptionBroadcast != null) {
			mSubscriptionBroadcast.unsubscribe();
		}
		if (mSubscriptionNewMessages != null) {
			mSubscriptionNewMessages.unsubscribe();
		}
		if (mSubscriptionChat != null) {
			mSubscriptionChat.unsubscribe();
		}
	}

	public void setNewInterval(long interval, long smallInterval) {
		if (interval != 0) {
			SpeakService.mInterval = interval;
			SpeakService.mSmallInterval = smallInterval;
		}
	}

	public void insertFragment(android.app.Fragment fragment) {
		getViewState().addFragment(fragment);
	}

	public void startActivity(Class<?> activity) {
		getViewState().showActivity(activity);
	}

	public void errorDialog(int idTitle, int idMessage, boolean clickListener) {
		getViewState().showErrorDialog(idTitle, idMessage, clickListener);
	}

	private boolean checkConnection() {
		ConnectivityManager cm = (ConnectivityManager) MyApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	private void handleErrorConnection() {
		if (!checkConnection()) {
			getViewState().showErrorDialog(R.string.error_connect_title, R.string.error_connect_message, false);
			getViewState().startGettingMessages(mNextPageToken);
		} else {
			getViewState().startGettingMessages(mNextPageToken);
		}
	}

	public void ttsErrorDialog() {
		getViewState().showTtsDialog();
	}

	public void goGooglePlay(String data, boolean forResult, int requestCode) {
		getViewState().goToMarket(data, forResult, requestCode);
	}

	public void setProfileData() {
		getViewState().setProfileData();
	}

	public void saveUserData(String photoUrl, String displayName, String email) {
		getViewState().saveUserData(photoUrl, displayName, email);
	}

	public void getAuthCode() {
		getViewState().startSignInActivity();
	}

	public void signOut() {
		getViewState().signOut();
	}
}

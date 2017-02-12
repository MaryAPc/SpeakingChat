package com.maryapc.speakingchat.presenter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.maryapc.speakingchat.MyApplication;
import com.maryapc.speakingchat.R;
import com.maryapc.speakingchat.SpeakService;
import com.maryapc.speakingchat.adapter.recycler.ChatListAdapter;
import com.maryapc.speakingchat.model.TokenResponse;
import com.maryapc.speakingchat.model.brooadcast.BroadcastResponse;
import com.maryapc.speakingchat.model.brooadcast.ItemsBroadcast;
import com.maryapc.speakingchat.model.chat.ChatResponse;
import com.maryapc.speakingchat.network.RetrofitService;
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
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@InjectViewState
public class ChatListPresenter extends MvpPresenter<ChatListView> {

	private Subscription mSubscriptionBroadast;
	private Subscription mSubscriptionChat;
	private Subscription mSubscriptionNewMessages;
	private ChatListAdapter mAdapter;

	private static String mLifeChatId = "";
	public static String mAccessToken = "";
	public static String mRefreshToken = "";
	public static String mTokenType = "";

	public static int mLastPlayPosition = 0;
	public static boolean isSpeakNow = false;
	private static int mItemCount;

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
				} else { //невалидный токен
					getNewAccessToken(false);
				}
			}

			@Override
			public void onFailure(Call<TokenResponse> call, Throwable t) {
			}
		});
	}

	private void getNewAccessToken(boolean isRunBroadcast) {
		OkHttpClient client = new OkHttpClient();
		RequestBody requestBody = new FormEncodingBuilder()
				.add("refresh_token", mRefreshToken)
				.add("client_id", MyApplication.getInstance().getString(R.string.android_client_id))
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
				Log.e("tag", e.toString());
			}

			@Override
			public void onResponse(Response response) throws IOException {
				try {
					JSONObject jsonObject = new JSONObject(response.body().string());
					final String message = jsonObject.toString(3);
					mAccessToken = jsonObject.get("access_token").toString();
					mTokenType = jsonObject.get("token_type").toString();
					getViewState().saveAccessToken(mAccessToken);
					if (!isRunBroadcast) {
						getViewState().startLifeBroadcast();
					}
					Log.d("tag", message + "new token");
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
				.add("client_id", MyApplication.getInstance().getString(R.string.android_client_id))
				.add("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
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
					final String message = jsonObject.toString(5);
					mAccessToken = jsonObject.get("access_token").toString();
					mTokenType = jsonObject.get("token_type").toString();
					mRefreshToken = jsonObject.get("refresh_token").toString();
					getViewState().saveTokens(mRefreshToken, mAccessToken, mTokenType);
					getViewState().startLifeBroadcast();
					Log.d("tag", message);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void getLifeBroadcast() {
		mSubscriptionBroadast = requestBroadcast()
				.subscribeOn(Schedulers.io())
				.map(BroadcastResponse::getItems)
				.doOnError(throwable -> {
					throwable.printStackTrace();
					Log.d("presenter", "DoError getLifeBroadcast");
					getNewAccessToken(true);
				})
				.retry(1)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<List<ItemsBroadcast>>() {
					boolean isEmptyBroadcast;
					String titleBroadcast = "";

					@Override
					public void onCompleted() {
						if (!isEmptyBroadcast) {
							getViewState().showConnectInfo(titleBroadcast);
							getViewState().showSpeechBar();
							getViewState().startLifeChat(mLifeChatId);
						}
					}

					@Override
					public void onError(Throwable e) {
						Log.d("presenter", "Error getLifeBroadcast");
						e.printStackTrace();
					}

					@Override
					public void onNext(List<ItemsBroadcast> itemses) {
						if (itemses.size() == 0) {
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
				.doOnError(throwable -> {
					throwable.printStackTrace();
					Log.d("presenter", "DoError getLifeChat");
					getNewAccessToken(true);
				})
				.retry(1)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<ChatResponse>() {
					String nextPageToken = "";

					@Override
					public void onCompleted() {
						getViewState().startGettingMessages(nextPageToken);
					}

					@Override
					public void onError(Throwable e) {
						Log.d("presenter", "Error getLifeChat");
					}

					@Override
					public void onNext(ChatResponse chatResponse) {
						nextPageToken = chatResponse.getNextPageToken();
						getViewState().setChatMessages(chatResponse.getItems());
					}
				});
	}

	public void getNextChatMessages(String nextPageToken) {
		mSubscriptionNewMessages = requestNextMessages(nextPageToken)
				.subscribeOn(Schedulers.io())
				.delay(2, TimeUnit.SECONDS)
				.retry(1)
				.doOnError(throwable -> {
					throwable.printStackTrace();
					Log.d("presenter", "DoError getNextChatMessages");
					getNewAccessToken(true);
				})
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<ChatResponse>() {
					String nextPageToken = "";

					@Override
					public void onCompleted() {
						//mItemCount = ChatListActivity.mChatListAdapter.getItemCount();
					}

					@Override
					public void onError(Throwable e) {
						Log.d("presenter", "Error getNextChatMessages");
					}

					@Override
					public void onNext(ChatResponse chatResponse) {
						nextPageToken = chatResponse.getNextPageToken();
						getViewState().addMessages(chatResponse.getItems(), nextPageToken);
					}
				});
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
}

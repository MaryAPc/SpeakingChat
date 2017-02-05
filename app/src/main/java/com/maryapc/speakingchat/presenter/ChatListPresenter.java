package com.maryapc.speakingchat.presenter;

import java.io.IOException;
import java.util.List;

import android.util.Log;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.maryapc.speakingchat.MyApplication;
import com.maryapc.speakingchat.R;
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

	private String mLifeChatId = "";

	public static String mAccessToken = "";
	public static String mRefreshToken = "";
	public static String mTokenType = "";

	public void visibleSignIn(boolean isSignIn) {
		getViewState().setVisibleSignIn(isSignIn);
	}

	public void checkToken() {
		Call<TokenResponse> responseCall = RetrofitService.getInstance().createApi().getTokenInfo(mAccessToken);
		responseCall.enqueue(new retrofit2.Callback<TokenResponse>() {
			@Override
			public void onResponse(Call<TokenResponse> call, retrofit2.Response<TokenResponse> response) {
				if (response.body().getError() == null) {
					getLifeBroadcast();
				} else { //невалидный токен
					Log.d("check_token", response.body().getError());
					getNewAccessToken();
				}
			}

			@Override
			public void onFailure(Call<TokenResponse> call, Throwable t) {

			}
		});
	}

	private void getNewAccessToken() {
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
					getLifeBroadcast();
					Log.d("tag", message);
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
					getLifeBroadcast();
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
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<List<ItemsBroadcast>>() {
					@Override
					public void onCompleted() {
						//getLifeChat(mLifeChatId);
					}

					@Override
					public void onError(Throwable e) {
						//обработать невалидный токен
						getNewAccessToken();
					}

					@Override
					public void onNext(List<ItemsBroadcast> itemses) {
						if (itemses.size() == 0) {
							getViewState().showEmptyBroadcast();
						}
						//mLifeChatId = itemses.get(0).getSnippet().getLiveChatId();
					}
				});

	}

	private void getLifeChat(String lifeChatId) {
		mSubscriptionChat = requestChat(lifeChatId)
				.subscribeOn(Schedulers.io())
				.subscribeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<ChatResponse>() {
					@Override
					public void onCompleted() {

					}

					@Override
					public void onError(Throwable e) {
						e.printStackTrace();
					}

					@Override
					public void onNext(ChatResponse chatResponse) {
						chatResponse.getItems();
					}
				});
	}

	private Observable<BroadcastResponse> requestBroadcast() {
		return RetrofitService.getInstance().createApi().getBroadcast(mTokenType + " " + mAccessToken, "snippet", "active", "all");
	}

	private Observable<ChatResponse> requestChat(String lifeChatId) {
		return RetrofitService.getInstance().createApi().getChat(mTokenType + " " + mAccessToken, lifeChatId, "snippet, authorDetails");
	}
}

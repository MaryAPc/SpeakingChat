package com.maryapc.speakingchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.Scope;
import com.maryapc.speakingchat.presenter.ChatListPresenter;
import com.maryapc.speakingchat.view.ChatListView;


public class ChatListActivity extends MvpAppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, ChatListView {

	@InjectPresenter
	ChatListPresenter mPresenter;

	private static final String TAG = "ChatListActivity";
	private static final int RC_GET_TOKEN = 1;

	private GoogleApiClient mGoogleApiClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_list);

		findViewById(R.id.activity_chat_list_button_sign_in).setOnClickListener(this);
		findViewById(R.id.activity_chat_list_button_sign_out).setOnClickListener(this);

		GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.server_client_id))
				.requestEmail()
				.requestScopes(new Scope("https://www.googleapis.com/auth/youtube.readonly"))
				.build();

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, this)
				.addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
				.build();
	}


	private void refreshIdToken() {
		OptionalPendingResult<GoogleSignInResult> pendingResult =
				Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
		if (pendingResult.isDone()) {
			GoogleSignInResult result = pendingResult.get();
			handleSignInResult(result);
		} else {
			pendingResult.setResultCallback(this::handleSignInResult);
		}
	}

	private void handleSignInResult(GoogleSignInResult result) {
		if (result.isSuccess()) {
			String idToken = result.getSignInAccount().getIdToken();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RC_GET_TOKEN) {
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			Log.d(TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());

			if (result.isSuccess()) {
				String idToken = result.getSignInAccount().getIdToken();
				// TODO(developer): send token to server and validate
			}
			handleSignInResult(result);
		}
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		Log.d(TAG, "onConnectionFailed:" + connectionResult);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.activity_chat_list_button_sign_in:
				mPresenter.signIn(mGoogleApiClient);
				break;
			case R.id.activity_chat_list_button_sign_out:
				mPresenter.signOut(mGoogleApiClient);
				break;
		}
	}

	@Override
	public void showAuth(GoogleApiClient apiClient) {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
		startActivityForResult(signInIntent, RC_GET_TOKEN);
	}

	@Override
	public void showSignInfo() {
		Toast.makeText(this, R.string.signed_out, Toast.LENGTH_SHORT).show();
	}
}
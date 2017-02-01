package com.maryapc.speakingchat.network;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

	private static RetrofitService sRetrofitService;
	private final Retrofit mRetrofit;

	public static RetrofitService getInstance() {
		if (sRetrofitService == null) {
			synchronized (RetrofitService.class) {
				if (sRetrofitService == null) {
					sRetrofitService = new RetrofitService();
				}
			}
		}
		return sRetrofitService;
	}

	public RetrofitService() {
		mRetrofit = new Retrofit.Builder()
				.baseUrl(GoogleApiUrls.BASE_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.build();
	}

	public ApiInterface createApi() {
		return mRetrofit.create(ApiInterface.class);
	}
}


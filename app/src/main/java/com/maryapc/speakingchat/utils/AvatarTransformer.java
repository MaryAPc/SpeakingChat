package com.maryapc.speakingchat.utils;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.maryapc.speakingchat.MyApplication;
import com.maryapc.speakingchat.adapter.CircleTransform;
import com.squareup.picasso.Picasso;

public class AvatarTransformer {

	@BindingAdapter({"android:src"})
	public static void transform(ImageView view, String avatarUrl) {
		if (!avatarUrl.equals("")) {
			Picasso.with(MyApplication.getInstance())
					.load(avatarUrl)
					.transform(new CircleTransform())
					.into(view);
		}
	}
}

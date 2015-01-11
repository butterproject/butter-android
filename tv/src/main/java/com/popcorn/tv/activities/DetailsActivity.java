package com.popcorn.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.popcorn.tv.R;
import com.popcorn.tv.fragments.VideoDetailsFragment;

import pct.droid.base.providers.media.types.Media;

public class DetailsActivity extends Activity implements VideoDetailsFragment.Callback {

	public static final String EXTRA_ITEM = "item";
	private Media mItem;

	public static Intent startActivity(Activity activity, Media item) {
		Intent intent = new Intent(activity, DetailsActivity.class);
		intent.putExtra(EXTRA_ITEM, item);
		activity.startActivity(intent);
		return intent;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

		mItem = getIntent().getParcelableExtra(EXTRA_ITEM);
	}

	@Override public Media getItem() {
		return mItem;
	}
}

package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import pct.droid.tv.R;
import pct.droid.tv.fragments.PTVMovieDetailsFragment;

import pct.droid.base.providers.media.types.Media;
import pct.droid.base.providers.media.types.Movie;

public class PTVMovieDetailActivity extends Activity implements PTVMovieDetailsFragment.Callback {

	public static final String EXTRA_ITEM = "item";
	public static final String SHARED_ELEMENT_NAME = "hero";
	private Media mItem;

	public static Intent startActivity(Activity activity, Movie item) {
		return startActivity(activity, null, item);
	}

	public static Intent startActivity(Activity activity, Bundle options, Movie item) {
		Intent intent = new Intent(activity, PTVMovieDetailActivity.class);
		intent.putExtra(EXTRA_ITEM, item);
		ActivityCompat.startActivity(activity, intent, options);
		return intent;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_movie_details);

		mItem = getIntent().getParcelableExtra(EXTRA_ITEM);
	}

	@Override public Media getItem() {
		return mItem;
	}
}

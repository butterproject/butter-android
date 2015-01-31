package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;
import pct.droid.tv.R;
import pct.droid.tv.fragments.PTVMovieDetailsFragment;


public class PTVShowDetailActivity extends Activity implements PTVMovieDetailsFragment.Callback {

	public static final String EXTRA_ITEM = "item";
	public static final String SHARED_ELEMENT_NAME = "hero";
	private Media mItem;

	public static Intent startActivity(Activity activity, Show item) {
		return startActivity(activity, null, item);
	}

	public static Intent startActivity(Activity activity, Bundle options, Show item) {
		Intent intent = new Intent(activity, PTVShowDetailActivity.class);
		intent.putExtra(EXTRA_ITEM, item);
		activity.startActivity(intent, options);
		return intent;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_details);

		mItem = getIntent().getParcelableExtra(EXTRA_ITEM);
	}

	@Override public Media getItem() {
		return mItem;
	}
}

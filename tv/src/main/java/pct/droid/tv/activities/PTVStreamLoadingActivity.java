package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;

import pct.droid.base.fragments.BaseStreamLoadingFragment;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;

public class PTVStreamLoadingActivity extends PTVBaseActivity implements BaseStreamLoadingFragment.FragmentListener {

	public final static String EXTRA_INFO = "mInfo";

	private StreamInfo mInfo;
	private BaseStreamLoadingFragment mFragment;

	public static Intent startActivity(Activity activity, StreamInfo info) {
		Intent i = new Intent(activity, PTVStreamLoadingActivity.class);
		i.putExtra(EXTRA_INFO, info);
		activity.startActivity(i);
		return i;
	}

	public static Intent startActivity(Activity activity, StreamInfo info, Pair<View, String>... elements) {
		Intent i = new Intent(activity, PTVStreamLoadingActivity.class);
		i.putExtra(EXTRA_INFO, info);

		ActivityOptionsCompat options =
				ActivityOptionsCompat.makeSceneTransitionAnimation(activity, elements);
		ActivityCompat.startActivity(activity, i, options.toBundle());
		return i;
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_streamloading);
		//todo: background manager

		if (!getIntent().hasExtra(EXTRA_INFO)) finish();

		mInfo = getIntent().getParcelableExtra(EXTRA_INFO);

		mFragment = (BaseStreamLoadingFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
	}

	@Override
	public StreamInfo getStreamInformation() {
		return mInfo;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		mFragment.cancelStream();
	}

	@Override public void onTorrentServiceDisconnected() {
		if (null != mFragment) {
			mFragment.onTorrentServiceDisconnected();
		}
	}

	@Override
	public void onTorrentServiceConnected() {
		if (null != mFragment) {
			mFragment.onTorrentServiceConnected();
		}
	}

}
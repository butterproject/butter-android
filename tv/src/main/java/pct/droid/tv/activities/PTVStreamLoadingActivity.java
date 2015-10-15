/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.tv.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;

import pct.droid.base.fragments.BaseStreamLoadingFragment;
import pct.droid.base.providers.media.models.Show;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;

public class PTVStreamLoadingActivity extends PTVBaseActivity implements BaseStreamLoadingFragment.FragmentListener {

	public final static String EXTRA_STREAM_INFO = "stream_info";
	public final static String EXTRA_SHOW_INFO = "show_info";

	private StreamInfo mInfo;
	private BaseStreamLoadingFragment mFragment;

	public static Intent startActivity(Activity activity, StreamInfo info) {
		Intent i = new Intent(activity, PTVStreamLoadingActivity.class);
		i.putExtra(EXTRA_STREAM_INFO, info);
		activity.startActivity(i);
		return i;
	}

	public static Intent startActivity(Activity activity, StreamInfo info, Show show) {
		Intent i = new Intent(activity, PTVStreamLoadingActivity.class);
		i.putExtra(EXTRA_STREAM_INFO, info);
		i.putExtra(EXTRA_SHOW_INFO, show);
		activity.startActivity(i);
		return i;
	}

	public static Intent startActivity(Activity activity, StreamInfo info, Pair<View, String>... elements) {
		Intent i = new Intent(activity, PTVStreamLoadingActivity.class);
		i.putExtra(EXTRA_STREAM_INFO, info);

		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
			activity, elements);
		ActivityCompat.startActivity(activity, i, options.toBundle());
		return i;
	}

	@SuppressLint("MissingSuperCall")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_streamloading);

		if (!getIntent().hasExtra(EXTRA_STREAM_INFO)) finish();
		mInfo = getIntent().getParcelableExtra(EXTRA_STREAM_INFO);
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

	@Override
    public void onTorrentServiceDisconnected() {
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
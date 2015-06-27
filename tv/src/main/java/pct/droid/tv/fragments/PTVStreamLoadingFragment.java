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

package pct.droid.tv.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.base.fragments.BaseStreamLoadingFragment;
import pct.droid.base.torrent.DownloadStatus;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.utils.ThreadUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.PTVVideoPlayerActivity;
import pct.droid.tv.utils.BackgroundUpdater;

public class PTVStreamLoadingFragment extends BaseStreamLoadingFragment {

	View mRoot;
	@InjectView(R.id.progressIndicator)
	ProgressBar progressIndicator;
	@InjectView(R.id.primary_textview)
	TextView mPrimaryTextView;
	@InjectView(R.id.secondary_textview)
	TextView mSecondaryTextView;
	@InjectView(R.id.tertiary_textview)
	TextView mTertiaryTextView;

	BackgroundUpdater mBackgroundUpdater = new BackgroundUpdater();

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		mRoot = inflater.inflate(R.layout.fragment_streamloading, container, false);
		ButterKnife.inject(this, mRoot);

		return mRoot;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mBackgroundUpdater.initialise(getActivity(), R.color.black);
		updateBackground();
	}

	private void updateStatus(final DownloadStatus status) {
		final DecimalFormat df = new DecimalFormat("#############0.00");
		ThreadUtils.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				progressIndicator.setIndeterminate(false);
				progressIndicator.setProgress(status.bufferProgress);
				mPrimaryTextView.setText(status.bufferProgress + "%");

				if (status.downloadSpeed / 1024 < 1000) {
					mSecondaryTextView.setText(df.format(status.downloadSpeed / 1024) + " KB/s");
				} else {
					mSecondaryTextView.setText(df.format(status.downloadSpeed / 1048576) + " MB/s");
				}
				mTertiaryTextView.setText(status.seeds + " " + getString(R.string.seeds));
			}
		});
	}


	@Override
	protected void updateView(State state, Object extra) {

		switch (state) {
			case UNINITIALISED:
				mTertiaryTextView.setText(null);
				mPrimaryTextView.setText(null);
				mSecondaryTextView.setText(null);
				progressIndicator.setIndeterminate(true);
				progressIndicator.setProgress(0);
				break;
			case ERROR:
				if (null != extra && extra instanceof String)
					mPrimaryTextView.setText((String) extra);
				mSecondaryTextView.setText(null);
				mTertiaryTextView.setText(null);
				progressIndicator.setIndeterminate(true);
				progressIndicator.setProgress(0);
				break;
			case BUFFERING:
				mPrimaryTextView.setText(R.string.starting_buffering);
				mTertiaryTextView.setText(null);
				mSecondaryTextView.setText(null);
				progressIndicator.setIndeterminate(true);
				progressIndicator.setProgress(0);
				break;
			case STREAMING:
				mPrimaryTextView.setText(R.string.streaming_started);
				if (null != extra && extra instanceof DownloadStatus)
					updateStatus((DownloadStatus) extra);
				break;
			case WAITING_SUBTITLES:
				mPrimaryTextView.setText(R.string.waiting_for_subtitles);
				mTertiaryTextView.setText(null);
				mSecondaryTextView.setText(null);
				progressIndicator.setIndeterminate(true);
				progressIndicator.setProgress(0);
				break;
			case WAITING_TORRENT:
				mPrimaryTextView.setText(R.string.waiting_torrent);
				mTertiaryTextView.setText(null);
				mSecondaryTextView.setText(null);
				progressIndicator.setIndeterminate(true);
				progressIndicator.setProgress(0);
				break;

		}
	}

	@Override protected void startPlayerActivity(String location, int resumePosition) {

		if (getActivity() != null && !mPlayerStarted) {
			mStreamInfo.setVideoLocation(location);
			PTVVideoPlayerActivity.startActivity(getActivity(), mStreamInfo, resumePosition);
		}
	}


	protected void updateBackground() {
		StreamInfo info = mCallback.getStreamInformation();
		  /* attempt to load background image */
		if (null != info) {
			String url = info.getHeaderImageUrl();

			mBackgroundUpdater.updateBackgroundAsync(url);
		}
	}
}

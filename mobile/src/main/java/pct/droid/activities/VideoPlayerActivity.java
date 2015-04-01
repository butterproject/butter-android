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

package pct.droid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import pct.droid.R;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.dialogfragments.OptionDialogFragment;
import pct.droid.fragments.VideoPlayerFragment;

public class VideoPlayerActivity extends BaseActivity implements VideoPlayerFragment.Callback {

    private StreamInfo mStreamInfo;
    private String mTitle = "";
    private VideoPlayerFragment mVideoPlayerFragment;

    public static Intent startActivity(Context context, StreamInfo info) {
        return startActivity(context, info, 0);
    }

    public static Intent startActivity(Context context, StreamInfo info, long resumePosition) {
        Intent i = new Intent(context, VideoPlayerActivity.class);
        i.putExtra(INFO, info);
        //todo: resume position
        context.startActivity(i);
        return i;
    }

    public final static String INFO = "stream_info";
    public final static String RESUME_POSITION = "resume_position";

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_videoplayer);

        mStreamInfo = getIntent().getParcelableExtra(INFO);

        if(mStreamInfo.isShow()) {
            mTitle = mStreamInfo.getShow().title;
        } else {
            if(mStreamInfo.getMedia() != null && mStreamInfo.getMedia().title != null)
                mTitle = mStreamInfo.getMedia().title;
        }

        String location = mStreamInfo.getVideoLocation();
        if(!location.startsWith("file://") && !location.startsWith("http://") && !location.startsWith("https://")) {
            location = "file://" + location;
        }
        mStreamInfo.setVideoLocation(location);

        mVideoPlayerFragment = (VideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.video_fragment);
        mVideoPlayerFragment.loadMedia();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
                showExitDialog();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public void onBackPressed() {
       showExitDialog();
    }

    private void showExitDialog() {
        OptionDialogFragment.show(getSupportFragmentManager(), getString(R.string.leave_videoplayer_title), String.format(getString(R.string.leave_videoplayer_message), mTitle), getString(android.R.string.yes), getString(android.R.string.no), new OptionDialogFragment.Listener() {
            @Override
            public void onSelectionPositive() {
                finish();
            }

            @Override
            public void onSelectionNegative() {}
        });
    }

	@Override
    public StreamInfo getInfo() {
		return mStreamInfo;
	}
}


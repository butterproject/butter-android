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
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import pct.droid.R;
import pct.droid.base.providers.media.models.Media;
import pct.droid.fragments.VideoPlayerFragment;

public class VideoPlayerActivity extends BaseActivity implements VideoPlayerFragment.Callback {

	private Media mMedia;
	private String mQuality;
	private String mSubtitleLanguage;
	private String mLocation;
    private VideoPlayerFragment mVideoPlayerFragment;

	public static Intent startActivity(Activity activity, String streamUrl, Media data) {
		return startActivity(activity, streamUrl, data, null, null, 0);
	}

	public static Intent startActivity(Activity activity, String streamUrl, Media data, String quality, String subtitleLanguage, long resumePosition) {
		Intent i = new Intent(activity, VideoPlayerActivity.class);
		i.putExtra(DATA, data);
		i.putExtra(QUALITY, quality);
		i.putExtra(SUBTITLES, subtitleLanguage);
		i.putExtra(LOCATION, streamUrl);
		//todo: resume position;
		activity.startActivity(i);
		return i;
	}

	public final static String LOCATION = "stream_url";
	public final static String DATA = "video_data";
	public final static String QUALITY = "quality";
	public final static String SUBTITLES = "subtitles";
	public final static String RESUME_POSITION = "resume_position";

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_videoplayer);

		mMedia = getIntent().getParcelableExtra(DATA);
		mQuality = getIntent().getStringExtra(QUALITY);
		mSubtitleLanguage = getIntent().getStringExtra(SUBTITLES);
		mLocation = getIntent().getStringExtra(LOCATION);

        mVideoPlayerFragment = (VideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.video_fragment);
        mVideoPlayerFragment.loadMedia();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
    public Media getData() {
		return mMedia;
	}

	@Override
    public String getQuality() {
		return mQuality;
	}

	@Override
    public String getSubtitles() {
		return mSubtitleLanguage;
	}

	@Override
    public String getLocation() {
		return mLocation;
	}
}


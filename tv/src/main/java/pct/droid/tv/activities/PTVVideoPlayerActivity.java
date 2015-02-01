package pct.droid.tv.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import pct.droid.base.providers.media.models.Media;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVVideoPlayerFragment;

public class PTVVideoPlayerActivity extends PTVBaseActivity implements PTVVideoPlayerFragment.Callback {

	private Media mMedia;
	private String mQuality;
	private String mSubtitleLanguage;
	private String mLocation;
	private PTVVideoPlayerFragment mFragment;

	public static Intent startActivity(Activity activity, String streamUrl, Media data) {
		return startActivity(activity, streamUrl, data, null, null, 0);
	}

	public static Intent startActivity(Activity activity, String streamUrl, Media data, String quality, String subtitleLanguage, long resumePosition) {
		Intent i = new Intent(activity, PTVVideoPlayerActivity.class);
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
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_videoplayer);

		mMedia = getIntent().getParcelableExtra(DATA);
		mQuality = getIntent().getStringExtra(QUALITY);
		mSubtitleLanguage = getIntent().getStringExtra(SUBTITLES);
		mLocation = getIntent().getStringExtra(LOCATION);

		mFragment = (PTVVideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
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

	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (null!=mFragment)return mFragment.onKeyDown(keyCode, event);
		return super.onKeyDown(keyCode, event);

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


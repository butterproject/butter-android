package pct.droid.tv.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MenuItem;

import pct.droid.base.providers.media.models.Media;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.torrent.TorrentService;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVVideoPlayerFragment;

public class PTVVideoPlayerActivity extends PTVBaseActivity implements PTVVideoPlayerFragment.Callback {

//	private Media mMedia;
//	private String mQuality;
//	private String mSubtitleLanguage;
//	private String mLocation;
	private PTVVideoPlayerFragment mFragment;

	public final static String INFO = "stream_info";

	private StreamInfo mStreamInfo;

	public static Intent startActivity(Context context, StreamInfo info) {
		return startActivity(context, info, 0);
	}

	public static Intent startActivity(Context context, StreamInfo info, long resumePosition) {
		Intent i = new Intent(context, PTVVideoPlayerActivity.class);
		i.putExtra(INFO, info);
		//todo: resume position
		context.startActivity(i);
		return i;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videoplayer);

		TorrentService.bindHere(this, mServiceConnection);

		mStreamInfo = getIntent().getParcelableExtra(INFO);


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

//
//	@Override
//    public String getSubtitles() {
//		return mSubtitleLanguage;
//	}
//
//	@Override
//    public String getLocation() {
//		return mLocation;
//	}

	@Override public StreamInfo getInfo() {
		return mStreamInfo;
	}

	@Override public TorrentService getService() {
		return mService;
	}

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((TorrentService.ServiceBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
	};
}


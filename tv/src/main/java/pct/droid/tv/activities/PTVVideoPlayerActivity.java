package pct.droid.tv.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.torrent.TorrentService;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVPlaybackOverlayFragment;
import pct.droid.tv.fragments.PTVVideoPlayerFragment;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PTVVideoPlayerActivity extends PTVBaseActivity implements PTVVideoPlayerFragment.Callback {

    private PTVVideoPlayerFragment mPlayerFragment;
    private PTVPlaybackOverlayFragment mPlaybackOverlayFragment;

    public final static String INFO = "stream_info";

    private StreamInfo mStreamInfo;
    private boolean mIsBackPressed = false;

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
    public void onBackPressed() {
        mIsBackPressed = true;
        super.onBackPressed();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_videoplayer);

        mStreamInfo = getIntent().getParcelableExtra(INFO);

        String location = mStreamInfo.getVideoLocation();
        if (!location.startsWith("file://") && !location.startsWith("http://") && !location.startsWith("https://")) {
            location = "file://" + location;
        }
        mStreamInfo.setVideoLocation(location);

        mPlayerFragment = (PTVVideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        mPlaybackOverlayFragment = (PTVPlaybackOverlayFragment) getSupportFragmentManager().findFragmentById(R.id.playback_overlay_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!mIsBackPressed && mPlayerFragment.isPlaying() && requestVisibleBehind(true)) {
            mPlayerFragment.setKeepEventBusRegistration(true);
            mPlaybackOverlayFragment.setKeepEventBusRegistration(true);
            super.onPause();
            return;
        }
        else {
            requestVisibleBehind(false);
            mPlayerFragment.setKeepEventBusRegistration(false);
            mPlaybackOverlayFragment.setKeepEventBusRegistration(false);
        }

        if (mService != null)
            mService.removeListener(mPlayerFragment);
        super.onPause();
    }

    @Override
    public void onVisibleBehindCanceled() {
        mPlayerFragment.pause();
        super.onVisibleBehindCanceled();
    }

    @Override
    protected void onDestroy() {
        if (mService != null)
            mService.stopStreaming();
        super.onDestroy();
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
    public StreamInfo getInfo() {
        return mStreamInfo;
    }

    @Override
    public TorrentService getService() {
        return mService;
    }

    @Override
    public void onTorrentServiceConnected() {
        mService.addListener(mPlayerFragment);
    }

    @Override
    public Long getResumePosition() {
        //todo: Implement ResumePosition on Android TV
        return 0L;
    }
}


package pct.droid.tv.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.torrent.TorrentService;
import pct.droid.base.utils.PrefUtils;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVPlaybackOverlayFragment;
import pct.droid.tv.fragments.PTVVideoPlayerFragment;

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
        super.onBackPressed();
        mIsBackPressed = true;
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
    protected void onPause() {
        super.onPause();

        if (mIsBackPressed) {
            mPlayerFragment.deactivateMediaSession();
        }

        if (mPlayerFragment.isMediaSessionActive()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                requestVisibleBehind(false);
            }
            mPlaybackOverlayFragment.setKeepEventBusRegistration(true);
            return;
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                requestVisibleBehind(false);
            }
            mPlaybackOverlayFragment.setKeepEventBusRegistration(false);
            PrefUtils.save(this, "resume_position", 0);
        }

        if (mService != null)
            mService.removeListener(mPlayerFragment);
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
    protected void onResume() {
        super.onResume();
        mIsBackPressed = false;
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


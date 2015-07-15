package pct.droid.tv.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.torrent.TorrentService;
import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;
import pct.droid.tv.fragments.PTVVideoPlayerFragment;

public class PTVVideoPlayerActivity extends PTVBaseActivity implements PTVVideoPlayerFragment.Callback {

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
        super.onCreate(savedInstanceState,R.layout.activity_videoplayer);

        mStreamInfo = getIntent().getParcelableExtra(INFO);


        String location = mStreamInfo.getVideoLocation();
        if (!location.startsWith("file://") && !location.startsWith("http://") && !location.startsWith("https://")) {
            location = "file://" + location;
        }
        mStreamInfo.setVideoLocation(location);

        mFragment = (PTVVideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
        mFragment.loadMedia();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (null != mFragment) {
            if (!mFragment.onKeyDown(keyCode, event)) {
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
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
        mService.addListener(mFragment);
    }

    @Override
    protected void onPause() {
        if(mService != null)
            mService.removeListener(mFragment);
        super.onPause();
    }

    @Override
    public Long getResumePosition() {
        //todo:
        return null;
    }
}


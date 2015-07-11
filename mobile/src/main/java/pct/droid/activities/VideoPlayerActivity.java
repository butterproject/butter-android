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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import pct.droid.R;
import pct.droid.activities.base.PopcornBaseActivity;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.torrent.TorrentService;
import pct.droid.dialogfragments.OptionDialogFragment;
import pct.droid.fragments.VideoPlayerFragment;

public class VideoPlayerActivity extends PopcornBaseActivity implements VideoPlayerFragment.Callback {

    public static final String RESUME_POSITION = "resume_position";

    private VideoPlayerFragment mFragment;
    private StreamInfo mStreamInfo;
    private String mTitle = "";
    private Long mResumePosition;

    public static Intent startActivity(Context context, StreamInfo info) {
        return startActivity(context, info, 0);
    }

    public static Intent startActivity(Context context, StreamInfo info, long resumePosition) {
        Intent i = new Intent(context, VideoPlayerActivity.class);
        i.putExtra(INFO, info);
        i.putExtra(RESUME_POSITION, resumePosition);
        context.startActivity(i);
        return i;
    }

    public final static String INFO = "stream_info";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_videoplayer);

        setShowCasting(true);

        mResumePosition = getIntent().getLongExtra(RESUME_POSITION, 0);
        mStreamInfo = getIntent().getParcelableExtra(INFO);

        mTitle = mStreamInfo.getTitle() == null ? getString(R.string.the_video) : mStreamInfo.getTitle();

        if(mStreamInfo == null) {
            finish();
            return;
        }

        mFragment = (VideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.video_fragment);
        mFragment.loadMedia();
    }

    @Override
    protected void onPause() {
        if(mService != null)
            mService.removeListener(mFragment);
        super.onPause();
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
                if (mService != null)
                    mService.stopStreaming();
                finish();
            }

            @Override
            public void onSelectionNegative() {
            }
        });
    }

    @Override
    public Long getResumePosition() {
        return mResumePosition;
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
    protected void onTorrentServiceConnected() {
        super.onTorrentServiceConnected();
        mService.addListener(mFragment);
    }

}


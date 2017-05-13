/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.tv.ui.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.providers.media.models.Show;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.activities.base.TVBaseActivity;
import butter.droid.tv.ui.loading.TVStreamLoadingActivity;
import butter.droid.tv.ui.player.video.TVPlayerFragment;
import butterknife.ButterKnife;
import javax.inject.Inject;

public class TVVideoPlayerActivity extends TVBaseActivity {

    public final static String EXTRA_STREAM_INFO = "butter.droid.tv.ui.player.TVVideoPlayerActivity.streamInfo";
    public final static String EXTRA_SHOW_INFO = "butter.droid.tv.ui.player.TVVideoPlayerActivity.episodeInfo";

    @Inject PrefManager prefManager;

    private TVVideoPlayerComponent component;

    private StreamInfo mStreamInfo;
    private boolean mIsBackPressed = false;
    private boolean mCurrentStreamStopped = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mIsBackPressed = true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = TVButterApplication.getAppContext()
                .getComponent()
                .tvVideoPlayerComponentBuilder()
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, 0);
//        super.onCreate(savedInstanceState, R.layout.activity_videoplayer);
        createStreamInfo();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, TVPlayerFragment.newInstance(mStreamInfo, 0))
                    .commit();
        }

        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsBackPressed = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

//        if (mIsBackPressed) {
//            mPlayerFragment.deactivateMediaSession();
//        }

    }

    @Override
    protected void onDestroy() {
        if (!mCurrentStreamStopped) {
            torrentStream.stopStreaming();
            mCurrentStreamStopped = true;
        }
        super.onDestroy();
    }

    @Override
    public void onVisibleBehindCanceled() {
//        mPlayerFragment.pause();
        super.onVisibleBehindCanceled();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTorrentServiceConnected(final TorrentService service) {
//        torrentStream.addListener(mPlayerFragment);
    }

    @Override
    public void onTorrentServiceDisconnected(final TorrentService service) {
//        torrentStream.removeListener(mPlayerFragment);
    }

    private void createStreamInfo() {
        mStreamInfo = getIntent().getParcelableExtra(EXTRA_STREAM_INFO);
        String location = mStreamInfo.getVideoLocation();

        if (!location.startsWith("file://") && !location.startsWith("http://") && !location.startsWith("https://")) {
            location = "file://" + location;
        }

        mStreamInfo.setVideoLocation(location);
    }

    public void skipTo(StreamInfo info, Show show) {
        torrentStream.stopStreaming();
        mCurrentStreamStopped = true;

//        mPlayerFragment.pause();
//        mPlayerFragment.onPlaybackEndReached();

        finish();

        TVStreamLoadingActivity.startActivity(this, info, show);
    }

    public TVVideoPlayerComponent getComponent() {
        return component;
    }

    public static Intent startActivity(Context context, StreamInfo info) {
        return startActivity(context, info, 0);
    }

    public static Intent startActivity(Context context, StreamInfo info, @SuppressWarnings("UnusedParameters") long resumePosition) {
        Intent i = new Intent(context, TVVideoPlayerActivity.class);
        i.putExtra(EXTRA_STREAM_INFO, info);
        // todo: resume position
        context.startActivity(i);
        return i;
    }

    public static Intent startActivity(Context context, StreamInfo info, Show show) {
        Intent i = new Intent(context, TVVideoPlayerActivity.class);
        i.putExtra(EXTRA_STREAM_INFO, info);
        i.putExtra(EXTRA_SHOW_INFO, show);
        // todo: resume position
        context.startActivity(i);
        return i;
    }
}

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
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.tv.ui.TVBaseActivity;
import butter.droid.tv.ui.player.video.TVPlayerFragment;

public class TVVideoPlayerActivity extends TVBaseActivity {

    private static final String EXTRA_STREAM_INFO = "butter.droid.tv.ui.player.TVVideoPlayerActivity.streamInfo";
    private static final String EXTRA_RESUME_POSITION = "butter.droid.tv.ui.player.TVVideoPlayerActivity.resumePosition";

    private static final String TAG_VIDEO_FRAGMENT = "butter.droid.tv.ui.player.TVVideoPlayerActivity.videoFragment";

    private TVPlayerFragment fragment;

    private StreamInfo streamInfo;
    private boolean currentStreamStopped = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createStreamInfo();

        if (savedInstanceState == null) {
            fragment = TVPlayerFragment.newInstance(streamInfo, 0);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment, TAG_VIDEO_FRAGMENT)
                    .commit();
        } else {
            fragment = (TVPlayerFragment) getSupportFragmentManager().findFragmentByTag(TAG_VIDEO_FRAGMENT);
        }

    }

    @Override
    protected void onDestroy() {
        if (!currentStreamStopped) {
            torrentStream.stopStreaming();
            currentStreamStopped = true;
        }
        super.onDestroy();
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
    public void onTorrentServiceDisconnected(final TorrentService service) {
        if (fragment != null) {
            service.removeListener(fragment);
        }

        super.onTorrentServiceDisconnected(service);
    }

    @Override
    public void onTorrentServiceConnected(final TorrentService service) {
        super.onTorrentServiceConnected(service);

        if (service.checkStopped()) {
            finish();
            return;
        }

        service.addListener(fragment);
    }

    private void createStreamInfo() {
        streamInfo = getIntent().getParcelableExtra(EXTRA_STREAM_INFO);

        String location = streamInfo.getStreamUrl();

        // TODO: 7/29/17 This should be handled earlier
        if (!location.startsWith("file://") && !location.startsWith("http://") && !location.startsWith("https://")) {
            location = "file://" + location;
        }

        streamInfo.setStreamUrl(location);
    }

    public static Intent startActivity(Context context, StreamInfo info, @SuppressWarnings("UnusedParameters") long resumePosition) {
        Intent intent = new Intent(context, TVVideoPlayerActivity.class);
        intent.putExtra(EXTRA_STREAM_INFO, info);
        intent.putExtra(EXTRA_RESUME_POSITION, resumePosition);
        context.startActivity(intent);
        return intent;
    }

    public static Intent startActivity(Context context, StreamInfo info) {
        Intent intent = new Intent(context, TVVideoPlayerActivity.class);
        intent.putExtra(EXTRA_STREAM_INFO, info);
        intent.putExtra(EXTRA_RESUME_POSITION, 0);
        context.startActivity(intent);
        return intent;
    }
}

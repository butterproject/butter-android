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

package butter.droid.ui.player;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.ui.player.fragment.PlayerFragment;
import butter.droid.fragments.dialog.OptionDialogFragment;
import butter.droid.ui.ButterBaseActivity;
import javax.inject.Inject;

public class VideoPlayerActivity extends ButterBaseActivity implements VideoPlayerView {

    private final static String EXTRA_STREAM_INFO = "butter.droid.ui.player.VideoPlayerActivity.streamInfo";
    private final static String EXTRA_RESUME_POSITION = "butter.droid.ui.player.VideoPlayerActivity.resumePosition";

    private final static String TAG_VIDEO_FRAGMENT = "butter.droid.ui.player.VideoPlayerActivity.videoFragment";

    @Inject VideoPlayerPresenter presenter;

    private VideoPlayerComponent component;
    private PlayerFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = MobileButterApplication.getAppContext()
                .getComponent()
                .videoPlayerComponentBuilder()
                .videoPlayerModule(new VideoPlayerModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, 0);

        setShowCasting(true);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        StreamInfo streamInfo = extras.getParcelable(EXTRA_STREAM_INFO);
        long resumePosition = extras.getLong(EXTRA_RESUME_POSITION, 0);

        if (savedInstanceState == null) {
            presenter.onCreate(streamInfo, resumePosition, intent.getAction(), intent);
        } else {
            fragment = (PlayerFragment) getSupportFragmentManager().findFragmentByTag(TAG_VIDEO_FRAGMENT);
        }

//        mTitle = mStreamInfo.getTitle() == null ? getString(R.string.the_video) : mStreamInfo.getTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TorrentService torrentService = getTorrentService();
        if (torrentService != null && torrentService.checkStopped()) {
            finish();
        }
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

    @Override public void showVideoFragment(@NonNull final StreamInfo streamInfo, final long resumePosition) {
        fragment = PlayerFragment.newInstance(streamInfo, resumePosition);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fragment, TAG_VIDEO_FRAGMENT)
                .commit();
    }

    public VideoPlayerComponent getComponent() {
        return component;
    }

    private void showExitDialog() {
        OptionDialogFragment.show(getSupportFragmentManager(), getString(R.string.leave_videoplayer_title),
//                String.format(getString(R.string.leave_videoplayer_message), mTitle), getString(android.R.string.yes),
                String.format(getString(R.string.leave_videoplayer_message), ""), getString(android.R.string.yes),
                getString(android.R.string.no), new OptionDialogFragment.Listener() {
                    @Override
                    public void onSelectionPositive() {
                        TorrentService torrentService = getTorrentService();
                        if (torrentService != null) {
                            torrentService.stopStreaming();
                        }
                        finish();
                    }

                    @Override
                    public void onSelectionNegative() {
                    }
                });
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

    public static Intent getIntent(Context context, @NonNull StreamInfo info) {
        return getIntent(context, info, 0);
    }

    public static Intent getIntent(Context context, @NonNull StreamInfo info, long resumePosition) {
        if (info == null) {
            throw new IllegalArgumentException("StreamInfo must not be null");
        }

        Intent i = new Intent(context, VideoPlayerActivity.class);
        i.putExtra(EXTRA_STREAM_INFO, info);
        i.putExtra(EXTRA_RESUME_POSITION, resumePosition);
        return i;
    }

}


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

package butter.droid.ui.beam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.activities.VideoPlayerActivity;
import butter.droid.base.manager.internal.beaming.server.BeamServerService;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.fragments.dialog.OptionDialogFragment;
import butter.droid.ui.ButterBaseActivity;
import butter.droid.ui.beam.fragment.BeamPlayerFragment;
import javax.inject.Inject;

public class BeamPlayerActivity extends ButterBaseActivity implements BeamPlayerActivityView {

    private final static String EXTRA_STREAM_INFO = "butter.droid.ui.beam.BeamPlayerActivity.streamInfo";
    private final static String EXTRA_RESUME_POSITION = "butter.droid.ui.beam.BeamPlayerActivity.resumePosition";

    private final static String TAG_FRAGMENT_BEAM = "butter.droid.ui.beam.fragment.BeamPlayerFragment";

    @Inject BeamPlayerActivityPresenter presenter;

    private BeamPlayerActivityComponent component;

    private BeamPlayerFragment fragment;
    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        component = MobileButterApplication.getAppContext()
                .getComponent()
                .beamPlayerActivityComponentBuilder()
                .beamPlayerActivityModule(new BeamPlayerActivityModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, 0);

        setShowCasting(true);

        StreamInfo streamInfo = getIntent().getParcelableExtra(EXTRA_STREAM_INFO);
        long resumePosition = getIntent().getLongExtra(EXTRA_RESUME_POSITION, 0);

        if (savedInstanceState == null) {
            fragment = BeamPlayerFragment.newInstance(streamInfo, resumePosition);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment, TAG_FRAGMENT_BEAM)
                    .commit();
        } else {
            fragment = (BeamPlayerFragment) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_BEAM);
        }

        BeamServerService.getServer().start();
        title = streamInfo.getTitle() == null ? getString(R.string.the_video) : streamInfo.getTitle();

        /*
        File subsLocation = new File(SubsProvider.getStorageLocation(context), media.videoId + "-" + subLanguage + ".srt");
        BeamServer.setCurrentSubs(subsLocation);
         */
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != torrentStream && torrentStream.checkStopped()) {
            finish();
        }
    }

    @Override
    protected void onStop() {
        if (null != torrentStream) {
            torrentStream.removeListener(fragment);
        }
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showExitDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    @Override
    public void onTorrentServiceConnected() {
        super.onTorrentServiceConnected();

        if (torrentStream.checkStopped()) {
            finish();
            return;
        }

        torrentStream.addListener(fragment);
    }

    @Override public void closePlayer() {
        if (torrentStream != null) {
            torrentStream.stopStreaming();
        }

        finish();
    }

    @Override public void fallbackToVideoPlayer(final StreamInfo streamInfo, final int resumePosition) {
        startActivity(VideoPlayerActivity.getIntent(this, streamInfo, resumePosition));
        closePlayer();
    }

    public BeamPlayerActivityComponent getComponent() {
        return component;
    }

    private void showExitDialog() {
        OptionDialogFragment.show(getSupportFragmentManager(), getString(R.string.leave_videoplayer_title),
                String.format(getString(R.string.leave_videoplayer_message), title),
                getString(android.R.string.yes), getString(android.R.string.no),
                new OptionDialogFragment.Listener() {
                    @Override
                    public void onSelectionPositive() {
                        presenter.stopVideo();
                    }

                    @Override
                    public void onSelectionNegative() {
                    }
                });
    }

    public static Intent getIntent(Context context, @NonNull StreamInfo info) {
        return getIntent(context, info, 0);
    }

    public static Intent getIntent(Context context, @NonNull StreamInfo info, long resumePosition) {
        if (info == null) {
            throw new IllegalArgumentException("StreamInfo must not be null");
        }

        Intent i = new Intent(context, BeamPlayerActivity.class);
        i.putExtra(EXTRA_STREAM_INFO, info);
        i.putExtra(EXTRA_RESUME_POSITION, resumePosition);
        return i;
    }

}

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

package butter.droid.tv.ui.trailer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.base.ui.dialog.DialogFactory;
import butter.droid.base.ui.dialog.DialogFactory.Action;
import butter.droid.base.ui.dialog.DialogFactory.ActionCallback;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.activities.base.TVBaseActivity;
import butter.droid.tv.ui.player.overlay.TVPlaybackOverlayFragment;
import butter.droid.tv.ui.player.video.TVVideoPlayerFragment;
import javax.inject.Inject;

public class TVTrailerPlayerActivity extends TVBaseActivity implements TVVideoPlayerFragment.Callback, TVTrailerPlayerView {

    private static final String EXTRA_LOCATION = "butter.droid.tv.ui.trailer.TVTrailerPlayerActivity.EXTRA_LOCATION";
    private static final String EXTRA_DATA = "butter.droid.tv.ui.trailer.TVTrailerPlayerActivity.EXTRA_DATA";

    @Inject
    TVTrailerPlayerPresenter presenter;

    private TVVideoPlayerFragment playerFragment;
    private TVPlaybackOverlayFragment tvPlaybackOverlayFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        TVButterApplication.getAppContext()
                .getComponent()
                .tvTrailerPlayerComponentBuilder()
                .tvTrailerModule(new TVTrailerPlayerModule(this))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_videoplayer);

        final Intent intent = getIntent();
        final Media media = intent.getParcelableExtra(EXTRA_DATA);
        final String youTubeUrl = intent.getStringExtra(EXTRA_LOCATION);

        final FragmentManager fm = getSupportFragmentManager();
        this.playerFragment = (TVVideoPlayerFragment) fm.findFragmentById(R.id.fragment);
        this.tvPlaybackOverlayFragment = (TVPlaybackOverlayFragment) fm.findFragmentById(R.id.playback_overlay_fragment);

        presenter.onCreate(media, youTubeUrl);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
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
    public Long getResumePosition() {
        return 0L;
    }

    @Override
    public StreamInfo getInfo() {
        return presenter.getStreamInfo();
    }

    @Override
    public TorrentService getService() {
        return null;
    }

    @Override
    public void onDisableVideoPlayerSubsButton() {
        tvPlaybackOverlayFragment.toggleSubtitleAction(false);
    }

    @Override
    public void onNotifyMediaReady() {
        playerFragment.onMediaReady();
    }

    @Override
    public void onDisplayErrorVideoDialog() {
        DialogFactory.createErrorFetchingYoutubeVideoDialog(this, new ActionCallback() {
            @Override
            public void onButtonClick(final Dialog which, final @Action int action) {
                finish();
            }
        }).show();
    }

    public static Intent getIntent(final Context context, final Media media, final String url) {
        final Intent intent = new Intent(context, TVTrailerPlayerActivity.class);
        intent.putExtra(EXTRA_DATA, media);
        intent.putExtra(EXTRA_LOCATION, url);
        return intent;
    }
}

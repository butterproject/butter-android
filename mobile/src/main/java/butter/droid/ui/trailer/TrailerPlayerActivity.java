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

package butter.droid.ui.trailer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.base.ui.dialog.DialogFactory;
import butter.droid.base.ui.dialog.DialogFactory.Action;
import butter.droid.base.ui.dialog.DialogFactory.ActionCallback;
import butter.droid.fragments.VideoPlayerFragment;
import butter.droid.ui.ButterBaseActivity;
import javax.inject.Inject;

public class TrailerPlayerActivity extends ButterBaseActivity implements TrailerPlayerView, VideoPlayerFragment.Callback {

    private final static String EXTRA_LOCATION = "butter.droid.ui.trailer.TrailerPlayerActivity.EXTRA_LOCATION";
    private final static String EXTRA_DATA = "butter.droid.ui.trailer.TrailerPlayerActivity.EXTRA_DATA";

    @Inject
    TrailerPlayerPresenter presenter;

    private VideoPlayerFragment videoPlayerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MobileButterApplication.getAppContext()
                .getComponent()
                .trailerComponentBuilder()
                .trailerModule(new TrailerPlayerModule(this))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_videoplayer);

        final Intent intent = getIntent();
        final Media media = intent.getParcelableExtra(EXTRA_DATA);
        final String youtubeUrl = intent.getStringExtra(EXTRA_LOCATION);

        this.videoPlayerFragment = (VideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.video_fragment);

        presenter.onCreate(media, youtubeUrl);
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
        }
        return super.onOptionsItemSelected(item);
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
        videoPlayerFragment.enableSubsButton(false);
    }

    @Override
    public void onNotifyMediaReady() {
        videoPlayerFragment.onMediaReady();
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
        final Intent intent = new Intent(context, TrailerPlayerActivity.class);
        intent.putExtra(TrailerPlayerActivity.EXTRA_DATA, media);
        intent.putExtra(TrailerPlayerActivity.EXTRA_LOCATION, url);
        return intent;
    }
}

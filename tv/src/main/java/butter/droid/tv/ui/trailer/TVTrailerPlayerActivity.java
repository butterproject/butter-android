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
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.ui.dialog.DialogFactory;
import butter.droid.base.ui.dialog.DialogFactory.Action;
import butter.droid.base.ui.dialog.DialogFactory.ActionCallback;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.activities.base.TVBaseActivity;
import butter.droid.tv.ui.trailer.fragment.TVTrailerPlayerFragment;
import javax.inject.Inject;

public class TVTrailerPlayerActivity extends TVBaseActivity implements TVTrailerPlayerView {

    private static final String EXTRA_LOCATION = "butter.droid.tv.ui.trailer.TVTrailerPlayerActivity.EXTRA_LOCATION";
    private static final String EXTRA_DATA = "butter.droid.tv.ui.trailer.TVTrailerPlayerActivity.EXTRA_DATA";

    private final static String TAG_VIDEO_FRAGMENT = "butter.droid.tv.ui.trailer.TVTrailerPlayerActivity.videoFragment";

    @Inject TVTrailerPlayerPresenter presenter;

    private TVTrailerPlayerComponent component;

    private TVTrailerPlayerFragment playerFragment;
//    private TVPlaybackOverlayFragment2 tvPlaybackOverlayFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = TVButterApplication.getAppContext()
                .getComponent()
                .tvTrailerPlayerComponentBuilder()
                .tvTrailerModule(new TVTrailerPlayerModule(this))
                .build();
        component.inject(this);

        super.onCreate(savedInstanceState, 0);

        final Intent intent = getIntent();
        final Media media = intent.getParcelableExtra(EXTRA_DATA);
        final String youtubeUrl = intent.getStringExtra(EXTRA_LOCATION);

        if (savedInstanceState == null) {
            playerFragment = TVTrailerPlayerFragment.newInstance(media, youtubeUrl);
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, playerFragment, TAG_VIDEO_FRAGMENT)
                    .commit();
//            presenter.onCreate(streamInfo, resumePosition, intent.getAction(), intent);
        } else {
            playerFragment = (TVTrailerPlayerFragment) getSupportFragmentManager().findFragmentByTag(TAG_VIDEO_FRAGMENT);
        }

//        this.playerFragment = (TVVideoPlayerFragment2) fm.findFragmentById(R.id.fragment);
//        this.tvPlaybackOverlayFragment = (TVPlaybackOverlayFragment2) fm.findFragmentById(R.id.playback_overlay_fragment);

//        presenter.onCreate(media, youTubeUrl);
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

//    @Override
//    public Long getResumePosition() {
//        return 0L;
//    }
//
//    @Override
//    public StreamInfo getInfo() {
//        return presenter.getStreamInfo();
//    }
//
//    @Override
//    public TorrentService getService() {
//        return null;
//    }

    @Override
    public void onDisableVideoPlayerSubsButton() {
//        tvPlaybackOverlayFragment.toggleSubtitleAction(false);
    }

    @Override
    public void onNotifyMediaReady() {
//        playerFragment.onMediaReady();
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

    public TVTrailerPlayerComponent getComponent() {
        return component;
    }

    public static Intent getIntent(final Context context, final Media media, final String url) {
        final Intent intent = new Intent(context, TVTrailerPlayerActivity.class);
        intent.putExtra(EXTRA_DATA, media);
        intent.putExtra(EXTRA_LOCATION, url);
        return intent;
    }
}

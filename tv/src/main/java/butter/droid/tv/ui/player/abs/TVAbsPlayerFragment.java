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

package butter.droid.tv.ui.player.abs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.VideoSupportFragment;
import android.support.v17.leanback.app.VideoSupportFragmentGlueHost;
import android.support.v17.leanback.media.MediaControllerGlue;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.ClosedCaptioningAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.media.session.PlaybackStateCompat.CustomAction;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.tv.BuildConfig;
import butter.droid.tv.R;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class TVAbsPlayerFragment extends VideoSupportFragment implements TVAbsPlayerView {

    protected static final String ARG_RESUME_POSITION = "butter.droid.tv.ui.player.abs.TVAbsPlayerFragment.resumePosition";

    @Inject TVAbsPlayerPresenter presenter;
    @Inject VlcPlayer player;

    protected MediaSessionCompat mediaSession;
    protected PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;
    private PlayerMediaControllerGlue mediaControllerGlue;

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaSession = new MediaSessionCompat(getContext(), BuildConfig.APPLICATION_ID);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setCallback(new PlayerSessionCallback());

        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_REWIND
                        | PlaybackStateCompat.ACTION_FAST_FORWARD
                        | PlaybackStateCompat.ACTION_SEEK_TO)
                .addCustomAction(PlayerMediaControllerGlue.ACTION_SCALE, getString(R.string.scale), R.drawable.ic_av_aspect_ratio);

        metadataBuilder = new MediaMetadataCompat.Builder();
    }

    @Override public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter.onViewCreated();
    }

    @Override public void onResume() {
        super.onResume();

        presenter.onResume();
    }

    @Override public void onPause() {
        super.onPause();

        presenter.onPause();
    }

    @Override public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        presenter.onSaveInstanceState(outState);
    }

    @Override public void onDestroy() {
        super.onDestroy();

        presenter.onDestroy();
    }

    @Override public void setupControls(final String title) {
        // TODO: 5/10/17 Define fast forward arrays

        mediaSession.setPlaybackState(stateBuilder.build());

        MediaControllerCompat mediaController = new MediaControllerCompat(getContext(), mediaSession);

        mediaControllerGlue = new PlayerMediaControllerGlue(getContext(), new int[]{ 1 }, new int[]{ 1 });
        mediaControllerGlue.attachToMediaController(mediaController);

        PlaybackControlsRowPresenter controlsRowPresenter = new PlaybackControlsRowPresenter(new DescriptionPresenter());
        controlsRowPresenter.setSecondaryActionsHidden(false);
        mediaControllerGlue.setControlsRowPresenter(controlsRowPresenter);

        mediaControllerGlue.setControlsRow(new PlaybackControlsRow(null));

        VideoSupportFragmentGlueHost videoSupportFragmentGlueHost = new VideoSupportFragmentGlueHost(this);
        mediaControllerGlue.setHost(videoSupportFragmentGlueHost);

        MediaControllerCompat.setMediaController(getActivity(), mediaController);

        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, title);
        mediaSession.setMetadata(metadataBuilder.build());
    }

    @Override public void attachVlcViews() {
        player.attachToSurface(getSurfaceView());
    }

    @Override public void showOverlay() {
        // nothing to do
    }

    @Override public void setProgressVisible(final boolean visible) {
        // nothing to do
    }

    @Override public void setKeepScreenOn(final boolean keep) {
        getSurfaceView().getHolder().setKeepScreenOn(keep);
    }

    @Override public void onPlaybackEndReached() {
        setKeepScreenOn(false);
    }

    @Override public void onErrorEncountered() {
        // TODO: 5/7/17
    }

    @Override public void updateControlsState(final boolean playing, final long currentTime, final long duration) {
        stateBuilder.setBufferedPosition(duration);
        stateBuilder.setState(playing ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, currentTime, 1);
        mediaSession.setPlaybackState(stateBuilder.build());

        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration);
        mediaSession.setMetadata(metadataBuilder.build());
    }

    @Override public void updateSurfaceSize(final int width, final int height) {
        SurfaceView surfaceView = getSurfaceView();
        SurfaceHolder holder = surfaceView.getHolder();
        holder.setFixedSize(width, height);

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = width;
        lp.height = height;
        surfaceView.setLayoutParams(lp);
    }

    @Override public void detachMediaSession() {
        mediaControllerGlue.detach();
        mediaSession.release();
    }

    @Override public void close() {
        getActivity().finish();
    }

    @Override public void saveState(final Bundle outState, final long resumePosition) {
        outState.putLong(ARG_RESUME_POSITION, resumePosition);
    }

    protected boolean onCustomAction(final String action, final Bundle extras) {
        switch (action) {
            case PlayerMediaControllerGlue.ACTION_SCALE:
                presenter.onScaleClicked();
                tickle();
                return true;
            default:
                return false;
        }
    }

    protected long getResumePosition(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return savedInstanceState.getLong(ARG_RESUME_POSITION);
        } else {
            return getArguments().getLong(ARG_RESUME_POSITION);
        }
    }

    public class PlayerSessionCallback extends MediaSessionCompat.Callback {

        @Override public void onPlay() {
            presenter.play();
        }

        @Override public void onPause() {
            presenter.pause();
        }

        @Override public void onSkipToNext() {

        }

        @Override public void onSkipToPrevious() {

        }

        @Override public void onFastForward() {
            presenter.seekForwardClick();
        }

        @Override public void onRewind() {
            presenter.seekBackwardClick();
        }

        @Override public void onCustomAction(final String action, final Bundle extras) {
            if (!TVAbsPlayerFragment.this.onCustomAction(action, extras)) {
                super.onCustomAction(action, extras);
            }
        }
    }

    /**
     * Detail presenter to allow showing movie or TV show details properly.
     */
    private static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {

        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            if (!(item instanceof StreamInfo)) {
                return;
            }

            StreamInfo streamInfo = (StreamInfo) item;

            if (streamInfo.hasParentMedia()) {
                viewHolder.getTitle().setText(streamInfo.getParentMediaTitle());
                viewHolder.getSubtitle().setText(streamInfo.getMediaTitle());

            } else {
                viewHolder.getTitle().setText(streamInfo.getFullTitle());
            }
        }

    }

    protected class PlayerMediaControllerGlue extends MediaControllerGlue {

        static final String ACTION_SCALE = "butter.droid.tv.ui.player.video.action.SCALE";
        public static final String ACTION_CLOSE_CAPTION = "butter.droid.tv.ui.player.video.action.CLOSE_CAPTION";

        private final List<String> actionsList = new ArrayList<>();

        /**
         * Constructor for the glue.
         *
         * @param fastForwardSpeeds Array of seek speeds for fast forward.
         * @param rewindSpeeds Array of seek speeds for rewind.
         */
        public PlayerMediaControllerGlue(final Context context, final int[] fastForwardSpeeds, final int[] rewindSpeeds) {
            super(context, fastForwardSpeeds, rewindSpeeds);
        }

        @Override protected void onStateChanged() {
            super.onStateChanged();

            PlaybackControlsRow controlsRow = getControlsRow();

            if (controlsRow == null) {
                return;
            }

            List<CustomAction> customActions = getMediaController().getPlaybackState().getCustomActions();

            ArrayObjectAdapter adapter = (ArrayObjectAdapter) controlsRow.getSecondaryActionsAdapter();

            if (customActions != null && customActions.size() > 0) {

                for (int i = 0; i < customActions.size(); i++) {

                    CustomAction customAction = customActions.get(i);

                    if (actionsList.size() == i || !customAction.getAction().equals(actionsList.get(i))) {
                        switch (customAction.getAction()) {
                            case ACTION_SCALE:
                                adapter.add(new Action(R.id.control_scale, customAction.getName(), null,
                                        ContextCompat.getDrawable(getContext(), customAction.getIcon())));
                                break;
                            case ACTION_CLOSE_CAPTION:
                                adapter.add(new ClosedCaptioningAction(getContext()));
                                break;
                            default:
                                // nothing to do
                                break;
                        }
                        actionsList.add(i, customAction.getAction());
                    }

                }

                if (actionsList.size() > customActions.size()) {
                    adapter.removeItems(actionsList.size() - 1, actionsList.size() - customActions.size());
                    for (int i = actionsList.size() - 1; i < customActions.size(); i++) {
                        actionsList.remove(i);
                    }
                }

            } else {
                adapter.clear();
            }

        }

        @Override public void onActionClicked(final Action action) {
            if (action.getId() == R.id.control_scale) {
                getMediaController().getTransportControls().sendCustomAction(ACTION_SCALE, null);
            } else if (action.getId() == R.id.lb_control_closed_captioning) {
                getMediaController().getTransportControls().sendCustomAction(ACTION_CLOSE_CAPTION, null);
            } else {
                super.onActionClicked(action);
            }
        }
    }

}

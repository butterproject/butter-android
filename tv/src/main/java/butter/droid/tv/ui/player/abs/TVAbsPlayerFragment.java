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
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.VideoSupportFragment;
import android.support.v17.leanback.app.VideoSupportFragmentGlueHost;
import android.support.v17.leanback.media.MediaControllerAdapter;
import android.support.v17.leanback.media.PlaybackTransportControlGlue;
import android.support.v17.leanback.media.PlayerAdapter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.ClosedCaptioningAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.PictureInPictureAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;

import javax.inject.Inject;

import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.tv.BuildConfig;

public class TVAbsPlayerFragment extends VideoSupportFragment implements TVAbsPlayerView {

    protected static final String ARG_RESUME_POSITION = "butter.droid.tv.ui.player.abs.TVAbsPlayerFragment.resumePosition";

    @Inject TVAbsPlayerPresenter presenter;
    @Inject VlcPlayer player;

    protected MediaSessionCompat mediaSession;
    protected PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;

    private SurfaceView subsSurface;

    private OnLayoutChangeListener surfaceLayoutListener;

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaSession = new MediaSessionCompat(requireContext(), BuildConfig.APPLICATION_ID);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setCallback(new PlayerSessionCallback());

        stateBuilder = new PlaybackStateCompat.Builder();

        metadataBuilder = new MediaMetadataCompat.Builder();
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        subsSurface = (SurfaceView) inflater.inflate(android.support.v17.leanback.R.layout.lb_video_surface, root,
                false);
        root.addView(subsSurface, 1); // above video view

        subsSurface.setZOrderMediaOverlay(true);
        subsSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        return root;
    }

    @Override public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter.onViewCreated();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();

        SurfaceView surfaceView = getSurfaceView();
        if (surfaceLayoutListener != null && surfaceView != null) {
            surfaceView.removeOnLayoutChangeListener(surfaceLayoutListener);
            surfaceLayoutListener = null;
        }
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
        mediaSession.setPlaybackState(stateBuilder.build());

        MediaControllerCompat mediaController = new MediaControllerCompat(getContext(), mediaSession);

        PlaybackTransportControlGlue<MediaControllerAdapter> mediaControllerGlue
                = new PlayerMediaControllerGlue<>(requireContext(), new MediaControllerAdapter(mediaController));
        mediaControllerGlue.setTitle(title);
        mediaControllerGlue.setControlsOverlayAutoHideEnabled(true);
        mediaControllerGlue.setSeekEnabled(true);

        VideoSupportFragmentGlueHost videoSupportFragmentGlueHost = new VideoSupportFragmentGlueHost(this);
        mediaControllerGlue.setHost(videoSupportFragmentGlueHost);

        MediaControllerCompat.setMediaController(requireActivity(), mediaController);

        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_TITLE, title);
        mediaSession.setMetadata(metadataBuilder.build());
    }

    @Override public void attachVlcViews() {
        SurfaceView surfaceView = getSurfaceView();
        player.attachToSurface(surfaceView, subsSurface);

        surfaceLayoutListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (left != oldLeft || top != oldTop || right != oldRight && bottom != oldBottom) {
                presenter.surfaceChanged(v.getWidth(), v.getHeight());
            }
        };
        surfaceView.addOnLayoutChangeListener(surfaceLayoutListener);
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
        stateBuilder.setState(playing ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                currentTime, 1);
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
//        mediaControllerGlue.detach();
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
//            case PlayerMediaControllerGlue.ACTION_SCALE:
//                presenter.onScaleClicked();
//                tickle();
//                return true;
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

        @Override public void onSeekTo(final long pos) {
            presenter.seekTo(pos);
        }

        @Override public void onSkipToNext() {
            // TODO handle
        }

        @Override public void onSkipToPrevious() {
            // TODO handle
        }

        @Override public void onCustomAction(final String action, final Bundle extras) {
            if (!TVAbsPlayerFragment.this.onCustomAction(action, extras)) {
                super.onCustomAction(action, extras);
            }
        }
    }

    protected class PlayerMediaControllerGlue<T extends PlayerAdapter> extends PlaybackTransportControlGlue<T> {

//        static final String ACTION_SCALE = "butter.droid.tv.ui.player.video.action.SCALE";

        private final PlaybackControlsRow.SkipPreviousAction skipPreviousAction;
        private final PlaybackControlsRow.SkipNextAction skipNextAction;

        private final PlaybackControlsRow.ClosedCaptioningAction ccAction;
        private final PlaybackControlsRow.PictureInPictureAction pipAction;

        /**
         * Constructor for the glue.
         *
         * @param context
         * @param impl Implementation to underlying media player.
         */
        public PlayerMediaControllerGlue(Context context, T impl) {
            super(context, impl);

            skipPreviousAction = new SkipPreviousAction(context);
            skipNextAction = new SkipNextAction(context);
            pipAction = new PictureInPictureAction(context);

            ccAction = new ClosedCaptioningAction(context);
        }

        @Override protected void onCreatePrimaryActions(ArrayObjectAdapter primaryActionsAdapter) {
            super.onCreatePrimaryActions(primaryActionsAdapter);
            primaryActionsAdapter.add(skipPreviousAction);
            primaryActionsAdapter.add(skipNextAction);
        }

        @Override protected void onCreateSecondaryActions(ArrayObjectAdapter secondaryActionsAdapter) {
            super.onCreateSecondaryActions(secondaryActionsAdapter);

            secondaryActionsAdapter.add(ccAction);
//            primaryActionsAdapter.add(pipAction); // TODO when implemented
            // TODO VLC wide scale action
        }

        @Override public void onActionClicked(Action action) {
            super.onActionClicked(action);

            // TODO for secondary actions
        }
    }

}

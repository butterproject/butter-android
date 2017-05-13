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

package butter.droid.tv.ui.player.overlay;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.PlaybackSupportFragment;
import android.support.v17.leanback.app.VideoSupportFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.BaseOnItemViewSelectedListener;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import butter.droid.tv.TVButterApplication;
import javax.inject.Inject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.MediaPlayer;

public class TVPlaybackOverlayFragment extends VideoSupportFragment implements BaseOnItemViewSelectedListener, OnActionClickedListener,
        Callback, IVLCVout.Callback {

    @Inject @Nullable LibVLC libVLC;

    private MediaPlayer mediaPlayer;

    private ArrayObjectAdapter rowsAdapter;
    private ArrayObjectAdapter primaryActionsAdapter;
    private PlaybackControlsRowPresenter playbackControlsRowPresenter;
    private PlaybackControlsRow playbackControlsRow;
    private PlayPauseAction playPauseAction;

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TVButterApplication.getAppContext()
                .getComponent()
                .inject(this);

        mediaPlayer = new MediaPlayer(libVLC);

        setSurfaceHolderCallback(this);

        setBackgroundType(PlaybackSupportFragment.BG_LIGHT);
        setOnItemViewSelectedListener(this);
        initialisePlaybackControlPresenter();
    }

    @Override public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupPlaybackControlItemsToInitialisingState();
    }

    @Override public void onItemSelected(final ViewHolder itemViewHolder, final Object item, final RowPresenter.ViewHolder rowViewHolder,
            final Object row) {

    }

    @Override public void onActionClicked(final Action action) {

    }

    @Override public SurfaceView getSurfaceView() {
        return super.getSurfaceView();
    }

    private void initialisePlaybackControlPresenter() {
        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
//        playbackControlsRowPresenter = new PlaybackControlsRowPresenter(new DescriptionPresenter());
//        playbackControlsRowPresenter.setSecondaryActionsHidden(false);

        presenterSelector.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        presenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());

        rowsAdapter = new ArrayObjectAdapter(presenterSelector);
        setAdapter(rowsAdapter);
    }

    private void setupPlaybackControlItemsToReadyState() {
        rowsAdapter.clear();
        playbackControlsRow = new PlaybackControlsRow(null); // TODO add stream info
        playbackControlsRow.setCurrentTime(0);
        playbackControlsRow.setBufferedProgress(0);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
//        setupPrimaryRowPlaybackControl(presenterSelector);
//        setupSecondaryRowPlaybackControl(presenterSelector);

        rowsAdapter.add(playbackControlsRow);
        rowsAdapter.notifyArrayItemRangeChanged(0, rowsAdapter.size());

        setupPlaybackControlItemsActions();
    }

    private void setupPlaybackControlItemsToInitialisingState() {
        rowsAdapter.clear();
        playbackControlsRow = new PlaybackControlsRow(null);
        playbackControlsRow.setCurrentTime(0);
        playbackControlsRow.setBufferedProgress(0);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        primaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        playbackControlsRow.setPrimaryActionsAdapter(primaryActionsAdapter);

        Activity activity = getActivity();
        playPauseAction = new PlayPauseAction(activity);
        primaryActionsAdapter.add(playPauseAction);

//        setupSecondaryRowPlaybackControl(presenterSelector);

        rowsAdapter.add(playbackControlsRow);
        rowsAdapter.notifyArrayItemRangeChanged(0, rowsAdapter.size());
    }

    private void setupPlaybackControlItemsActions() {
        playbackControlsRowPresenter.setOnActionClickedListener(this);
    }

    @Override public void surfaceCreated(final SurfaceHolder holder) {
        final IVLCVout vlcVout = mediaPlayer.getVLCVout();
        vlcVout.setVideoView(getSurfaceView());
        vlcVout.addCallback(this);
        vlcVout.attachViews();
    }

    @Override public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {

    }

    @Override public void surfaceDestroyed(final SurfaceHolder holder) {

    }

    @Override
    public void onNewLayout(final IVLCVout vlcVout, final int width, final int height, final int visibleWidth, final int visibleHeight,
            final int sarNum, final int sarDen) {

    }

    @Override public void onSurfacesCreated(final IVLCVout vlcVout) {

    }

    @Override public void onSurfacesDestroyed(final IVLCVout vlcVout) {

    }

}

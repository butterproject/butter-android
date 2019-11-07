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

package butter.droid.tv.ui.player.video;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import org.butterproject.torrentstream.StreamStatus;
import org.butterproject.torrentstream.Torrent;
import org.butterproject.torrentstream.listeners.TorrentListener;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.ui.player.stream.StreamPlayerPresenterImpl;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.tv.ui.player.abs.TVAbsPlayerFragment;
import dagger.android.support.AndroidSupportInjection;

public class TVPlayerFragment extends TVAbsPlayerFragment implements TVPlayerView, TorrentListener {

    private static final String ARG_STREAM_INFO = "butter.droid.tv.ui.player.video.TVPlayerFragment.streamInfo";

    @Inject TVPlayerPresenter presenter;

    @Override public void onAttach(final Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StreamInfo streamInfo = getArguments().getParcelable(ARG_STREAM_INFO);
        long resumePosition = getResumePosition(savedInstanceState);

//        stateBuilder.addCustomAction(PlayerMediaControllerGlue.ACTION_CLOSE_CAPTION, getString(R.string.subtitles),
// R.drawable.ic_av_subs);

        presenter.onCreate(streamInfo, resumePosition);
    }

    @Override public void displayStreamProgress(final int progress) {
        stateBuilder.setBufferedPosition(progress);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override public void showErrorMessage(@StringRes final int message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override public void showSubsSelectorDialog() {
        // TODO: 5/7/17 - will be implemented later
    }

    @Override public void showPickSubsDialog(MediaWrapper mediaWrapper, @Nullable Subtitle subtitle) {
        // TODO
    }

    @Override public void showSubsFilePicker() {
        // TODO: 5/7/17 - will be implemented later
    }

    @Override public void displaySubsTimingDialog(final int subtitleOffset) {
        // TODO: 5/7/17 - will be implemented later
    }

    @Override protected boolean onCustomAction(final int action) {
        switch (action) {
            case StreamPlayerPresenterImpl.PLAYER_ACTION_CC:
                presenter.onSubsClicked();
                tickle();
                return true;
            default:
                return super.onCustomAction(action);
        }
    }

    @Override public void onStreamPrepared(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamStarted(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamError(final Torrent torrent, final Exception ex) {
        // nothing to do
    }

    @Override public void onStreamReady(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamProgress(final Torrent torrent, final StreamStatus streamStatus) {
        presenter.streamProgressUpdated(streamStatus.progress);
    }

    @Override public void onStreamStopped() {
        // nothing to do
    }

    public static TVPlayerFragment newInstance(@NonNull StreamInfo streamInfo, long resumePosition) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_STREAM_INFO, streamInfo);
        args.putLong(ARG_RESUME_POSITION, resumePosition);

        TVPlayerFragment fragment = new TVPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

}

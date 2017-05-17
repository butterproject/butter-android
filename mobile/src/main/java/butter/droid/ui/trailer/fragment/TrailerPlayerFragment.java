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

package butter.droid.ui.trailer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import butter.droid.R;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.ui.beam.BeamPlayerActivity;
import butter.droid.ui.player.abs.AbsPlayerFragment;
import butter.droid.ui.trailer.TrailerPlayerActivity;
import butter.droid.widget.StrokedRobotoTextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import javax.inject.Inject;

public class TrailerPlayerFragment extends AbsPlayerFragment implements TrailerPlayerView, TorrentListener {

    private final static String ARG_URI = "butter.droid.ui.trailer.fragment.TrailerPlayerFragment.uri";
    private final static String ARG_MEDIA = "butter.droid.ui.trailer.fragment.TrailerPlayerFragment.media";

    @Inject TrailerPlayerPresenter presenter;

    @BindView(R.id.subtitle_text) StrokedRobotoTextView subtitleText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        TrailerPlayerActivity activity = (TrailerPlayerActivity) getActivity();
        activity.getComponent()
                .trailerPlayerComponentBuilder()
                .trailerPlayerModule(new TrailerPlayerModule(this, activity))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String uri = args.getString(ARG_URI);
        Media media = args.getParcelable(ARG_MEDIA);

        presenter.onCreate(media, uri);
    }

    @Override public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        subtitleText.setVisibility(View.GONE);
    }

    @Override public void displayStreamProgress(final int progress) {
        stateBuilder.setBufferedPosition(progress);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    @Override public void showVolumeMessage(final int volume) {
        showPlayerInfo(getString(R.string.volume) + '\u00A0' + Integer.toString(volume));
    }

    @Override public void startBeamPlayerActivity(@NonNull final StreamInfo streamInfo, final long currentTime) {
        getActivity().startActivity(BeamPlayerActivity.getIntent(getActivity(), streamInfo, currentTime));
    }

    @Override public void onStreamPrepared(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamStarted(final Torrent torrent) {
        // nothing to do
    }

    @Override public void onStreamError(final Torrent torrent, final Exception e) {
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

    public static TrailerPlayerFragment newInstance(final Media media, final String trailerUri) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_MEDIA, media);
        args.putString(ARG_URI, trailerUri);

        TrailerPlayerFragment fragment = new TrailerPlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

}

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

package butter.droid.ui.player.stream;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View.OnSystemUiVisibilityChangeListener;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.ui.player.VideoPlayerActivity;
import butter.droid.ui.player.abs.AbsPlayerFragment;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;

public class PlayerFragment extends AbsPlayerFragment implements PlayerView, OnSystemUiVisibilityChangeListener, TorrentListener {

    private static final String ARG_STREAM_INFO = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.streamInfo";
    private static final String ARG_RESUME_POSITION = "butter.droid.base.ui.player.fragment.BaseVideoPlayerFragment.resumePosition";

    private static final String ACTION_SCALE = "butter.droid.tv.ui.player.video.action.SCALE";
    private static final String ACTION_CLOSE_CAPTION = "butter.droid.tv.ui.player.video.action.CLOSE_CAPTION";

    private static final int FADE_OUT_INFO = 1000;
    public static final int SUBTITLE_MINIMUM_SIZE = 10;

    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mediaController;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;

    private int lastSystemUIVisibility;
    private boolean overlayVisible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        VideoPlayerActivity activity = (VideoPlayerActivity) getActivity();
        activity.getComponent()
                .playerComponentBuilder()
                .playerModule(new PlayerModule(this, activity))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState);
    }

    public static PlayerFragment newInstance(final StreamInfo streamInfo, final long resumePosition) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_STREAM_INFO, streamInfo);
        args.putLong(ARG_RESUME_POSITION, resumePosition);

        PlayerFragment fragment = new PlayerFragment();
        fragment.setArguments(args);
        return fragment;
    }

}

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

package butter.droid.ui.loading.fragment;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.github.sv244.torrentstream.Torrent;

import butter.droid.activities.BeamPlayerActivity;
import butter.droid.activities.VideoPlayerActivity;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.beaming.BeamManager;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.manager.vlc.PlayerManager;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment.State;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragmentPresenterImpl;
import butter.droid.base.utils.FragmentUtil;
import butter.droid.base.utils.PixelUtils;

public class StreamLoadingFragmentPresenterImpl extends BaseStreamLoadingFragmentPresenterImpl
        implements StreamLoadingFragmentPresenter {

    private final StreamLoadingFragmentView view;
    private final Context context;
    private final BeamManager beamManager;

    private Torrent currentTorrent;

    public StreamLoadingFragmentPresenterImpl(StreamLoadingFragmentView view, ProviderManager providerManager,
            PreferencesHandler preferencesHandler, PlayerManager playerManager, Context context,
            BeamManager beamManager) {
        super(view, providerManager, preferencesHandler, playerManager, context);
        this.view = view;
        this.context = context;
        this.beamManager = beamManager;
    }

    @Override public void onResume() {
        super.onResume();

        if (playingExternal) {
            setState(State.STREAMING);
        }

        loadBackgroundImage();
    }

    @Override protected void updateView(State state, Object extra) {

    }

    @Override protected void startPlayerActivity(String location, int resumePosition) {
        this.streamInfo.setVideoLocation(location);
        if (beamManager.isConnected()) {
            view.startBeamActivity(streamInfo, resumePosition);
        } else {
            playingExternal = playerManager.start(this.streamInfo.getMedia(), this.streamInfo.getSubtitleLanguage(),
                    location);
            if (!playingExternal) {
                VideoPlayerActivity.startActivity(context, this.streamInfo, resumePosition);
            }
        }

        if (!playingExternal) {
            view.closeSelf();
        } else {
            mStartExternalButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStreamPrepared(Torrent torrent) {
        currentTorrent = torrent;

        if (TextUtils.isEmpty(this.streamInfo.getTitle())) {
            view.pickTorrentFile(currentTorrent.getFileNames());
        } else {
            super.onStreamPrepared(currentTorrent);
        }
    }

    private void loadBackgroundImage() {
        if (null != streamInfo) {
            String url = streamInfo.getImageUrl();
            if (PixelUtils.isTablet(context)) {
                url = streamInfo.getHeaderImageUrl();
            }

            if (!TextUtils.isEmpty(url)) {
                view.loadBackgroundImage(url);
            }
        }
    }

    @Override public void selectTorrentFile(int position) {
        currentTorrent.setSelectedFile(position);
        onStreamPrepared(currentTorrent);
    }
}

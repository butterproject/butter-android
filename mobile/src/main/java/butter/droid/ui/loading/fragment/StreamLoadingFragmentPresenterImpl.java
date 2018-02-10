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
import android.content.Intent;
import android.text.TextUtils;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.subtitle.SubtitleManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment.State;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragmentPresenterImpl;
import butter.droid.base.utils.PixelUtils;
import org.butterproject.torrentstream.Torrent;

public class StreamLoadingFragmentPresenterImpl extends BaseStreamLoadingFragmentPresenterImpl
        implements StreamLoadingFragmentPresenter {

    private final StreamLoadingFragmentView view;
    private final Context context;
    private final BeamManager beamManager;

    private Torrent currentTorrent;

    public StreamLoadingFragmentPresenterImpl(StreamLoadingFragmentView view, ProviderManager providerManager,
            SubtitleManager subtitleManager, PlayerManager playerManager, Context context, BeamManager beamManager) {
        super(view, providerManager, subtitleManager, playerManager, context);
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

    @Override protected void startPlayerActivity(int resumePosition) {
        if (beamManager.isConnected()) {
            view.startBeamActivity(streamInfo, resumePosition);
        } else {
            Intent intent = playerManager.externalPlayerIntent(streamInfo);

            if (intent != null) {
                playingExternal = true;
                view.startExternalPlayer(intent);
            } else {
                playingExternal = false;
                view.startPlayerActivity(streamInfo, resumePosition);
            }
        }

        if (!playingExternal) {
            view.closeSelf();
        } else {
            view.showExternalPlayerButton();
        }
    }

    @Override
    public void onStreamPrepared(Torrent torrent) {
        currentTorrent = torrent;

        // TODO: 7/29/17 This does not make sense
        if (TextUtils.isEmpty(this.streamInfo.getFullTitle())) {
            view.pickTorrentFile(currentTorrent.getFileNames());
        } else {
            super.onStreamPrepared(currentTorrent);
        }
    }

    private void loadBackgroundImage() {
        if (null != streamInfo) {
            final String url;
            if (PixelUtils.isTablet(context)) {
                url = streamInfo.getBackdropImage();
            } else {
                url = streamInfo.getPosterImage();
            }

            if (!TextUtils.isEmpty(url)) {
                view.loadBackgroundImage(url);
            }
        }
    }

    @Override public void selectTorrentFile(int position) {
        currentTorrent.setSelectedFileIndex(position);
        onStreamPrepared(currentTorrent);
    }

    @Override public void startExternalPlayer() {
        Intent intent = playerManager.externalPlayerIntent(streamInfo);
        if (intent != null) {
            view.startExternalPlayer(intent);
        } else {
            // TODO: 3/1/17 Notify user
        }
    }
}

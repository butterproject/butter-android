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

package butter.droid.base.ui.loading.fragment;

import android.content.Context;

import com.github.sv244.torrentstream.StreamStatus;
import com.github.sv244.torrentstream.Torrent;
import com.github.sv244.torrentstream.listeners.TorrentListener;

import java.util.Map;

import butter.droid.base.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.beaming.server.BeamServer;
import butter.droid.base.manager.beaming.server.BeamServerService;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.manager.vlc.PlayerManager;
import butter.droid.base.providers.media.models.Episode;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Movie;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.subs.SubtitleDownloader;
import butter.droid.base.subs.TimedTextObject;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment.State;
import butter.droid.base.utils.ThreadUtils;

public abstract class BaseStreamLoadingFragmentPresenterImpl implements BaseStreamLoadingFragmentPresenter,
        TorrentListener, SubtitleDownloader.ISubtitleDownloaderListener, SubsProvider.Callback{

    private final BaseStreamLoadingFragmentView view;
    private final ProviderManager providerManager;
    private final PreferencesHandler preferencesHandler;
    protected final PlayerManager playerManager;
    private final Context context;

    private enum SubsStatus {SUCCESS, FAILURE, DOWNLOADING}

    protected StreamInfo streamInfo;

    private State state;
    private boolean hasSubs = false;
    private String videoLocation;
    private SubsStatus subsStatus = SubsStatus.DOWNLOADING;
    private String subtitleLanguage = null;
    protected boolean playingExternal = false;
    protected Boolean playerStarted = false;

    public BaseStreamLoadingFragmentPresenterImpl(BaseStreamLoadingFragmentView view, ProviderManager providerManager,
            PreferencesHandler preferencesHandler, PlayerManager playerManager, Context context) {
        this.view = view;
        this.providerManager = providerManager;
        this.preferencesHandler = preferencesHandler;
        this.playerManager = playerManager;
        this.context = context;
    }

    @Override public void onCreate(StreamInfo streamInfo) {
        this.streamInfo = streamInfo; // TODO: 2/16/17 Check if not null

        loadSubtitles();
    }

    @Override public void onResume() {
        if (playerStarted) {
            BeamServer beamService = BeamServerService.getServer();
            if (beamService != null) {
                beamService.stop();
            }
            if (!playingExternal) {
                view.backPressed();
            }
        }

        if (state == null) {
            setState(State.WAITING_TORRENT);
        } else {
            setState(state);
        }
    }

    /**
     * Starts the torrent service streaming a torrent url
     */
    @Override public void startStream() {
        String torrentUrl = streamInfo.getTorrentUrl();

        view.startStreamUrl(torrentUrl);
    }

    @Override public void onStreamPrepared(Torrent torrent) {
        torrent.startDownload();
    }

    @Override public void onStreamStarted(Torrent torrent) {
        setState(State.BUFFERING);
    }

    @Override public void onStreamError(Torrent torrent, Exception e) {
        if (e.getMessage().equals("Write error")) {
            setState(State.ERROR, context.getString(R.string.error_files));
        } else if (e.getMessage().equals("Torrent error")) {
            setState(State.ERROR, context.getString(R.string.torrent_failed));
        } else {
            setState(State.ERROR, context.getString(R.string.unknown_error));
        }

    }

    @Override public void onStreamReady(Torrent torrent) {
        videoLocation = torrent.getVideoFile().toString();
        startPlayer(videoLocation);
    }

    @Override public void onStreamProgress(Torrent torrent, StreamStatus status) {
        if (!videoLocation.isEmpty()) {
            startPlayer(videoLocation);
        }

        setState(State.STREAMING, status);
    }

    @Override public void onStreamStopped() {
        // nothing to do
    }

    @Override public void onSuccess(Map<String, String> items) {
        Media media = streamInfo.getMedia();
        media.subtitles = items;

        subsStatus = SubsStatus.SUCCESS;
        hasSubs = false;

        if (media.subtitles == null || media.subtitles.size() == 0) {
            return;
        }

        if (streamInfo.getSubtitleLanguage() == null) {
            String language = preferencesHandler.getSubtitleDefaultLanguage();
            if (media.subtitles.containsKey(language)) {
                streamInfo.setSubtitleLanguage(language);
            } else {
                streamInfo.setSubtitleLanguage(SubsProvider.SUBTITLE_LANGUAGE_NONE);
            }
        }

        if (streamInfo.getSubtitleLanguage() != null && !streamInfo.getSubtitleLanguage().equals(
                SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
            subtitleLanguage = streamInfo.getSubtitleLanguage();
            subsStatus = SubsStatus.DOWNLOADING;
            hasSubs = true;
            SubtitleDownloader subtitleDownloader = new SubtitleDownloader(providerManager.getCurrentSubsProvider(), streamInfo, playerManager, subtitleLanguage);
            subtitleDownloader.downloadSubtitle();
        }
    }

    @Override public void onFailure(Exception e) {
        subsStatus = SubsStatus.FAILURE;
    }

    @Override public void onSubtitleDownloadCompleted(boolean isSuccessful, TimedTextObject subtitleFile) {
        subsStatus = isSuccessful ? SubsStatus.SUCCESS : SubsStatus.FAILURE;
    }

    protected void setState(final State state) {
        setState(state, null);
    }

    protected void setState(final State state, final Object extra) {
        this.state = state;

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView(state, extra);
            }
        });
    }

    /**
     * Update the view based on a state.
     *
     * @param state
     * @param extra - an optional extra piece of data relating to the state, such as an error message, or status data
     */
    protected abstract void updateView(State state, Object extra);

    protected abstract void startPlayerActivity(String location, , int resumePosition);

    /**
     * Starts the player for a torrent stream.
     * <p/>
     * Will either start an external player, or the internal one
     */
    private void startPlayer(String location) {
        if (hasSubs && subsStatus == SubsStatus.DOWNLOADING) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setState(State.WAITING_SUBTITLES);
                }
            });
            return;
        }

        if (!playerStarted) {
            startPlayerActivity(location, 0);
//            view.startPlayerActivity(location);

            playerStarted = true;
        }
    }

    /**
     * Downloads the subs file
     */
    private void loadSubtitles() {
        Media media = streamInfo.getMedia();
        if (media == null) {
            return;
        }

        SubsProvider subsProvider = providerManager.getCurrentSubsProvider();
        if (subsProvider == null) {
            return;
        }

        if (streamInfo.isShow()) {
            subsProvider.getList((Episode) media, this);
        } else {
            subsProvider.getList((Movie) media, this);
        }
    }
}


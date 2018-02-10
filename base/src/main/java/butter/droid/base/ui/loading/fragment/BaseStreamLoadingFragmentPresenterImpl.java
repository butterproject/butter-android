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
import butter.droid.base.R;
import butter.droid.base.manager.internal.beaming.server.BeamServer;
import butter.droid.base.manager.internal.beaming.server.BeamServerService;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.subtitle.SubtitleManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment.State;
import butter.droid.base.utils.StringUtils;
import butter.droid.base.utils.ThreadUtils;
import butter.droid.provider.subs.SubsProvider;
import org.butterproject.torrentstream.StreamStatus;
import org.butterproject.torrentstream.Torrent;
import org.butterproject.torrentstream.listeners.TorrentListener;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;

public abstract class BaseStreamLoadingFragmentPresenterImpl implements BaseStreamLoadingFragmentPresenter, TorrentListener {

    private final BaseStreamLoadingFragmentView view;
    private final ProviderManager providerManager;
    private final SubtitleManager subtitleManager;
    protected final PlayerManager playerManager;
    private final Context context;

    protected StreamInfo streamInfo;

    private State state;
    @Nullable private Disposable subtitleDisposable;
    protected boolean playingExternal = false;
    protected Boolean playerStarted = false;

    public BaseStreamLoadingFragmentPresenterImpl(BaseStreamLoadingFragmentView view, ProviderManager providerManager,
            final SubtitleManager subtitleManager, PlayerManager playerManager, Context context) {
        this.view = view;
        this.providerManager = providerManager;
        this.subtitleManager = subtitleManager;
        this.playerManager = playerManager;
        this.context = context;
    }

    public void onCreate(StreamInfo streamInfo) {
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
        streamInfo.setStreamUrl(torrent.getVideoFile().toString());
        startPlayer();
    }

    @Override public void onStreamProgress(Torrent torrent, StreamStatus status) {
        setState(State.STREAMING, status);
    }

    @Override public void onStreamStopped() {
        // TODO should probably do something here?
        // nothing to do
    }

    protected void setState(final State state) {
        setState(state, null);
    }

    protected void setState(final State state, final Object extra) {
        this.state = state;

        ThreadUtils.runOnUiThread(() -> updateView(state, extra));
    }

    /**
     * Update the view based on a state.
     *
     * @param extra - an optional extra piece of data relating to the state, such as an error message, or status data
     */
    protected void updateView(State state, Object extra) {
        // TODO: 11/14/17 This should be nicer
        switch (state) {
            case UNINITIALISED:
                view.displayPrimaryText(null);
                view.clearTexts();
                break;
            case ERROR:
                if (null != extra && extra instanceof String) {
                    view.displayPrimaryText((String) extra);
                }
                view.clearTexts();
                break;
            case BUFFERING:
                view.displayPrimaryText(R.string.starting_buffering);
                view.clearTexts();
                break;
            case STREAMING:
                if (null != extra && extra instanceof StreamStatus) {
                    updateStatus((StreamStatus) extra);
                }
                break;
            case WAITING_SUBTITLES:
                view.displayPrimaryText(R.string.waiting_for_subtitles);
                view.clearTexts();
                break;
            case WAITING_TORRENT:
                view.displayPrimaryText(R.string.waiting_torrent);
                view.clearTexts();
                break;
            default:
                throw new IllegalArgumentException("Unknown state");

        }

    }

    protected abstract void startPlayerActivity(int resumePosition);

    /**
     * Starts the player for a torrent stream.
     * <p/>
     * Will either start an external player, or the internal one
     */
    private void startPlayer() {
        if (streamInfo.hasSubtitles() && subtitleDisposable != null && !subtitleDisposable.isDisposed()) {
            return;
        }

        if (StringUtils.isEmpty(streamInfo.getStreamUrl())) {
            return;
        }

        startPlayerActivity(0);
        playerStarted = true;
    }

    /**
     * Downloads the subs file
     */
    private void loadSubtitles() {
        MediaWrapper media = streamInfo.getMedia();
        SubsProvider subsProvider = providerManager.getSubsProvider(media.getProviderId());
        SubtitleWrapper subtitle = streamInfo.getSubtitle();

        subtitleManager.downloadSubtitle(subsProvider, media.getMedia(), subtitle)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MaybeObserver<SubtitleWrapper>() {
                    @Override public void onSubscribe(final Disposable d) {
                        subtitleDisposable = d;
                    }

                    @Override public void onSuccess(final SubtitleWrapper wrapper) {
                        subtitleDisposable = null;
                        startPlayer();
                    }

                    @Override public void onError(final Throwable e) {
                        subtitleDisposable = null;
                        startPlayer();
                    }

                    @Override public void onComplete() {
                        subtitleDisposable = null;
                        startPlayer();
                    }
                });
    }

    private void updateStatus(final StreamStatus status) {
        int progress;
        if (!playingExternal) {
            progress = status.bufferProgress;
        } else {
            progress = ((Float) status.progress).intValue();
        }

        String progressText = progress + "%";

        String speedText;
        if (status.downloadSpeed / 1024 < 1000) {
            int i = (int) (status.downloadSpeed / 102.4);
            speedText = i / 10 + "." + i % 10 + " KB/s";
        } else {
            int i = (int) (status.downloadSpeed / 104857.6);
            speedText = i / 10 + "." + i % 10 + " MB/s";
        }

        String seedsText = status.seeds + " " + context.getString(R.string.seeds);

        view.displayDetails(progress, progressText, speedText, seedsText);
    }

}


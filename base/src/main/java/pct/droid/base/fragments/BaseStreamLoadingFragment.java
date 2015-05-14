/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import hugo.weaving.DebugLog;
import pct.droid.base.R;
import pct.droid.base.activities.TorrentBaseActivity;
import pct.droid.base.beaming.server.BeamServerService;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.torrent.DownloadStatus;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.torrent.TorrentService;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.ThreadUtils;


/**
 * This fragment handles starting a stream of a torrent.
 * <p/>
 * <p/>
 * It does multiple things:
 * <p/>
 * <pre>
 * 1. Downloads torrent file
 * 2. Downloads subtitles file
 * (1 and 2 happen asynchronously. Generally the torrent will take much longer than downloading subs)
 *
 * 3. Starts downloading (buffering) the torrent
 * 4. Starts the Video activity
 * </pre>
 * <p/>
 * <p/>
 * <p/>
 * //todo: most of this logic should probably be factored out into its own service at some point
 */
public abstract class BaseStreamLoadingFragment extends Fragment implements TorrentService.Listener {

    protected FragmentListener mCallback;
    private SubsProvider mSubsProvider;
    protected Boolean mPlayerStarted = false;
    private Boolean mHasSubs = false;
    private TorrentService mService;

    protected StreamInfo mStreamInfo;

    private enum SubsStatus {SUCCESS, FAILURE, DOWNLOADING}

    private SubsStatus mSubsStatus = SubsStatus.DOWNLOADING;
    private String mSubtitleLanguage = null, mVideoLocation = "";

    public enum State {
        UNINITIALISED, WAITING_TORRENT, WAITING_SUBTITLES, BUFFERING, STREAMING, ERROR;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStreamInfo = mCallback.getStreamInformation();
                loadSubs();
            }
        });

        if (!(getActivity() instanceof TorrentBaseActivity)) return;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentListener) mCallback = (FragmentListener) activity;
    }

    public void onTorrentServiceConnected() {
        mService = ((TorrentBaseActivity) getActivity()).getTorrentService();
        mService.addListener(this);
        startStream();
    }

    public void onTorrentServiceDisconnected() {
        if(mService != null) {
            mService.removeListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mService) {
            mService.removeListener(this);
        }
    }

    /**
     * Update the view based on a state.
     *
     * @param state
     * @param extra - an optional extra piece of data relating to the state, such as an error message, or status data
     */
    protected abstract void updateView(State state, Object extra);

    /**
     * Start the internal player for a streaming torrent
     *
     * @param location
     * @param resumePosition
     */
    protected abstract void startPlayerActivity(String location, int resumePosition);

    @DebugLog
    private void setState(final State state) {
        setState(state, null);
    }

    @DebugLog
    private void setState(final State state, final Object extra) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateView(state, extra);
            }
        });
    }

    /**
     * Starts the player for a torrent stream.
     * <p/>
     * Will either start an external player, or the internal one
     */
    @DebugLog
    private void startPlayer(String location) {
        if (mHasSubs && mSubsStatus == SubsStatus.DOWNLOADING) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setState(State.WAITING_SUBTITLES);
                }
            });
            return;
        }

        if (!mPlayerStarted) {
            mService.removeListener(BaseStreamLoadingFragment.this);
            startPlayerActivity(location, 0);

            mPlayerStarted = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayerStarted) {
            BeamServerService.getServer().stop();
            getActivity().onBackPressed();
        }

        if(mService != null && mService.isReady()) {
            onStreamReady(mService.getCurrentVideoLocation());
        }

        setState(State.WAITING_TORRENT);
    }

    /**
     * Starts the torrent service streaming a torrent url
     */
    private void startStream() {
        if (null == mService) throw new IllegalStateException("Torrent service must be bound");
        String torrentUrl = mStreamInfo.getTorrentUrl();

        //if the torrent service is currently streaming another file, stop it.
        if (mService.isStreaming() && !mService.getCurrentTorrentUrl().equals(torrentUrl)) {
            mService.stopStreaming();
        }
        //start streaming the new file
        mService.streamTorrent(torrentUrl);
    }

    /**
     * Stops the torrent service streaming
     */
    @DebugLog
    public void cancelStream() {
        if (mService != null) {
            mService.stopStreaming();
        }
    }

    @Override
    @DebugLog
    public void onStreamStarted() {
        setState(State.BUFFERING);
    }

    @Override
    @DebugLog
    public void onStreamError(final Exception e) {
        if (e.getMessage().equals("Write error")) {
            setState(State.ERROR, getString(R.string.error_files));
        } else if (e.getMessage().equals("Torrent error")) {
            setState(State.ERROR, getString(R.string.torrent_failed));
        } else {
            setState(State.ERROR, getString(R.string.unknown_error));
        }
    }

    /**
     * Called when torrent buffering has reached 100%
     *
     * @param videoLocation
     */
    @Override
    @DebugLog
    public void onStreamReady(File videoLocation) {
        mVideoLocation = videoLocation.toString();
        startPlayer(mVideoLocation);
    }


    /**
     * Called when the torrent buffering status has been updated
     *
     * @param status
     */
    @Override
    @DebugLog
    public void onStreamProgress(DownloadStatus status) {
        if (mVideoLocation.isEmpty()) {
            setState(State.STREAMING, status);
        } else {
            startPlayer(mVideoLocation);
        }
    }


    /**
     * Downloads the subs file
     */
    private void loadSubs() {
        final Media data = mStreamInfo.getMedia();
        if (null != data) {

            //if there are no subtitles specified, try to use the default subs
            if (mStreamInfo.getSubtitleLanguage() == null && data.subtitles != null && data.subtitles.size() > 0) {
                if (data.subtitles.containsKey(PrefUtils.get(getActivity(), Prefs.SUBTITLE_DEFAULT, "no-subs"))) {
                    mStreamInfo.setSubtitleLanguage(PrefUtils.get(getActivity(), Prefs.SUBTITLE_DEFAULT, "no-subs"));
                }
            }

            //todo: tidy up
            mSubsStatus = SubsStatus.SUCCESS;

            //load subtitles
            if (data.subtitles != null && data.subtitles.size() > 0 && mStreamInfo.getSubtitleLanguage() != null) {
                mHasSubs = true;
                mSubtitleLanguage = mStreamInfo.getSubtitleLanguage();
                if (!mSubtitleLanguage.equals("no-subs")) {
                    SubsProvider.download(getActivity(), data, mSubtitleLanguage, new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            mSubsStatus = SubsStatus.FAILURE;
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            mSubsStatus = SubsStatus.SUCCESS;
                        }
                    });
                } else {
                    mSubsStatus = SubsStatus.SUCCESS;
                }
            } else {
                mSubsProvider = data.getSubsProvider();
                if (null != mSubsProvider) {
                    SubsProvider.Callback subsCallback = new SubsProvider.Callback() {
                        @Override
                        public void onSuccess(Map<String, String> items) {
                            data.subtitles = items;
                            mSubsStatus = SubsStatus.SUCCESS;
                        }

                        @Override
                        public void onFailure(Exception e) {
                            mSubsStatus = SubsStatus.FAILURE;
                        }
                    };

                    if (mStreamInfo.isShow()) {
                        mSubsProvider.getList(mStreamInfo.getShow(), (Episode) data, subsCallback);
                    } else {
                        mSubsProvider.getList((Movie) data, subsCallback);
                    }
                }
            }
        }
    }

    public interface FragmentListener {
        StreamInfo getStreamInformation();
    }

}

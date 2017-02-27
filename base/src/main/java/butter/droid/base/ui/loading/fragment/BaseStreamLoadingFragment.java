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

import android.os.Bundle;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.torrent.TorrentService;


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
public abstract class BaseStreamLoadingFragment extends Fragment implements BaseStreamLoadingFragmentView {

    protected static final String ARGS_STREAM_INFO = "butter.droid.fragments.StreamLoadingFragment.streamInfo";

    @Inject BaseStreamLoadingFragmentPresenter presenter;

    private TorrentService service;

    public enum State {
        UNINITIALISED, WAITING_TORRENT, WAITING_SUBTITLES, BUFFERING, STREAMING, ERROR;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // TODO: 2/16/17 Is needed?

        StreamInfo streamInfo = getArguments().getParcelable(ARGS_STREAM_INFO);
        presenter.onCreate(streamInfo);
    }

    @Override public void startStreamUrl(String torrentUrl) {
        if (null == service) {
            throw new IllegalStateException("Torrent service must be bound");
        }

        //if the torrent service is currently streaming another file, stop it.
        if (service.isStreaming() && !service.getCurrentTorrentUrl().equals(torrentUrl)) {
            service.stopStreaming();
        } else if (service.isReady()) {
            presenter.onStreamReady(service.getCurrentTorrent());
        }

        //start streaming the new file
        service.streamTorrent(torrentUrl);
    }

    public void onTorrentServiceConnected(TorrentService service) {
        if (getActivity() == null) {
            return;
        }

        this.service = service;
        if (this.service != null) {
            this.service.addListener(presenter);
            presenter.startStream();
        }
    }

    public void onTorrentServiceDisconnected() {
        if (service != null) {
            service.removeListener(presenter);
            service = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != service) {
            service.removeListener(presenter);
            service = null;
        }
    }

    @Override public void backPressed() {
        getActivity().onBackPressed();
    }

//    @Override public void startPlayerActivity(String location) {
//        service.removeListener(presenter);
//        startPlayerActivity(location, 0);
//    }
//
//    /**
//     * Start the internal player for a streaming torrent
//     *
//     * @param location
//     * @param resumePosition
//     */
//    protected abstract void startPlayerActivity(String location, int resumePosition);

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();

        if (service != null && service.isStreaming() && service.isReady()) {
            presenter.onStreamReady(service.getCurrentTorrent());
        }
    }

    /**
     * Stops the torrent service streaming
     */
    public void cancelStream() {
        if (service != null) {
            service.stopStreaming();
        }
    }

}

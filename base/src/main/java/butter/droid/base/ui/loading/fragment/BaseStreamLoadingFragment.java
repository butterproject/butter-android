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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import butter.droid.base.R;
import butter.droid.base.torrent.TorrentService;
import dagger.android.support.DaggerFragment;


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
public abstract class BaseStreamLoadingFragment extends DaggerFragment implements BaseStreamLoadingFragmentView {

    protected static final String ARGS_STREAM_INFO = "butter.droid.fragments.StreamLoadingFragment.streamInfo";

    @Inject BaseStreamLoadingFragmentPresenter presenter;

    static class ViewHolder {
        ProgressBar progressIndicator;
        TextView primaryTextView;
        TextView secondaryTextView;
        TextView tertiaryTextView;

        private void bind(View view) {
            progressIndicator = view.findViewById(R.id.progress_indicator);
            primaryTextView = view.findViewById(R.id.primary_textview);
            secondaryTextView = view.findViewById(R.id.secondary_textview);
            tertiaryTextView = view.findViewById(R.id.tertiary_textview);
        }
    }

    private final ViewHolder viewHolder = new ViewHolder();

    private TorrentService service;

    public enum State {
        UNINITIALISED, WAITING_TORRENT, WAITING_SUBTITLES, BUFFERING, STREAMING, ERROR
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewHolder.bind(view);
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

    @Override public void displayPrimaryText(String text) {
        viewHolder.primaryTextView.setText(text);
    }

    @Override public void displayPrimaryText(@StringRes int text) {
        viewHolder.primaryTextView.setText(text);
    }

    @Override public void clearTexts() {
        viewHolder.secondaryTextView.setText(null);
        viewHolder.tertiaryTextView.setText(null);
        viewHolder.progressIndicator.setIndeterminate(true);
        viewHolder.progressIndicator.setProgress(0);
    }

    @Override public void displayDetails(int progress, String progressText, String speedText, String seedsText) {
        viewHolder.progressIndicator.setIndeterminate(false);
        viewHolder.progressIndicator.setProgress(progress);

        viewHolder.primaryTextView.setText(progressText);
        viewHolder.secondaryTextView.setText(speedText);
        viewHolder.tertiaryTextView.setText(seedsText);
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

    @Override public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
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

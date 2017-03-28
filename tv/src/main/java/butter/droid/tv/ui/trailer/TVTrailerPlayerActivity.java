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

package butter.droid.tv.ui.trailer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MenuItem;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.youtube.YouTubeManager;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.activities.base.TVBaseActivity;
import butter.droid.tv.fragments.TVPlaybackOverlayFragment;
import butter.droid.tv.fragments.TVVideoPlayerFragment;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import javax.inject.Inject;
import timber.log.Timber;

public class TVTrailerPlayerActivity extends TVBaseActivity implements TVVideoPlayerFragment.Callback, TVTrailerPlayerView {

    public static final String LOCATION = "stream_url";
    public static final String DATA = "video_data";

    private static final String TAG = TVTrailerPlayerActivity.class.getSimpleName();

    @Inject
    TVTrailerPlayerPresenter presenter;
    @Inject
    YouTubeManager youTubeManager;
    @Inject
    NetworkManager networkManager;

    private TVVideoPlayerFragment playerFragment;
    private TVPlaybackOverlayFragment tvPlaybackOverlayFragment;

    public static Intent startActivity(Context context, String youTubeUrl, Media data) {
        Intent intent = new Intent(context, TVTrailerPlayerActivity.class);
        intent.putExtra(DATA, data);
        intent.putExtra(LOCATION, youTubeUrl);
        context.startActivity(intent);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        TVButterApplication.getAppContext()
                .getComponent()
                .tvTrailerPlayerComponentBuilder()
                .tvTrailerModule(new TVTrailerPlayerModule(this))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_videoplayer);

        final Media media = getIntent().getParcelableExtra(DATA);
        media.title += " " + getString(R.string.trailer);
        final String youTubeUrl = getIntent().getStringExtra(LOCATION);

        final FragmentManager fm = getSupportFragmentManager();
        this.playerFragment = (TVVideoPlayerFragment) fm.findFragmentById(R.id.fragment);
        this.tvPlaybackOverlayFragment = (TVPlaybackOverlayFragment) fm.findFragmentById(R.id.playback_overlay_fragment);

        presenter.onCreate(media, youTubeUrl);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Long getResumePosition() {
        return 0L;
    }

    @Override
    public StreamInfo getInfo() {
        return presenter.getStreamInfo();
    }

    @Override
    public TorrentService getService() {
        return null;
    }

    @Override
    public void onDisableVideoPlayerSubsButton() {
        tvPlaybackOverlayFragment.toggleSubtitleAction(false);
    }

    @Override
    public void onExecuteQueryYoutubeTask(String youtubeUrl) {
        final QueryYouTubeTask youTubeTask = new QueryYouTubeTask(this, youTubeManager, networkManager);
        final String youTubeVideoId = youTubeManager.getYouTubeVideoId(youtubeUrl);
        youTubeTask.execute(youTubeVideoId);
    }

    @Override
    public void onNotifyMediaReady() {
        playerFragment.onMediaReady();
    }

    @Override
    public void onDisplayErrorVideoDialog() {
        try {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TVTrailerPlayerActivity.this);
            alertDialogBuilder.setTitle(R.string.comm_error);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setMessage(R.string.comm_message);

            alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TVTrailerPlayerActivity.this.finish();
                }
            });

            final AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Problem showing error dialog", e);
        }
    }

    private static class QueryYouTubeTask extends AsyncTask<String, Void, Uri> {

        private static final String TAG = QueryYouTubeTask.class.getSimpleName();

        private final YouTubeManager youTubeManager;
        private final NetworkManager networkManager;

        private final WeakReference<TVTrailerPlayerActivity> activityWeakReference;

        private QueryYouTubeTask(final TVTrailerPlayerActivity activity, final YouTubeManager youTubeManager,
                final NetworkManager networkManager) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.youTubeManager = youTubeManager;
            this.networkManager = networkManager;
        }

        @Override
        protected Uri doInBackground(String... params) {
            final String videoId = params[0];
            try {
                int videoQuality;
                if (networkManager.isWifiConnected() || networkManager.isEthernetConnected()) {
                    videoQuality = YouTubeManager.QUALITY_HIGH_MP4;
                } else {
                    videoQuality = YouTubeManager.QUALITY_MEDIUM_3GPP;
                }
                // calculate the actual URL of the video, encoded with proper YouTube token
                final String uriStr = youTubeManager.calculateYouTubeUrl(videoQuality, true, videoId);
                return Uri.parse(uriStr);
            } catch (Exception e) {
                Timber.e(TAG, "Error occurred while retrieving information from YouTube", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri result) {
            final TVTrailerPlayerActivity trailerPlayerActivity = activityWeakReference.get();
            if (isCancelled() || trailerPlayerActivity == null || trailerPlayerActivity.isFinishing()) {
                return;
            }

            if (result != null) {
                final String videoUrl = URLDecoder.decode(result.toString());
                trailerPlayerActivity.presenter.onVideoUrlObtained(videoUrl);
            } else {
                trailerPlayerActivity.presenter.onErrorObtainingVideoUrl();
            }
        }

    }

}

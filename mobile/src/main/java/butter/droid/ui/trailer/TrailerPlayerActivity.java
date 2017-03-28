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

package butter.droid.ui.trailer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.phone.PhoneManager;
import butter.droid.base.manager.youtube.YouTubeManager;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.torrent.TorrentService;
import butter.droid.fragments.VideoPlayerFragment;
import butter.droid.ui.ButterBaseActivity;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import javax.inject.Inject;
import timber.log.Timber;

public class TrailerPlayerActivity extends ButterBaseActivity implements TrailerPlayerView, VideoPlayerFragment.Callback {

    public final static String LOCATION = "stream_url";
    public final static String DATA = "video_data";

    private static final String TAG = TrailerPlayerActivity.class.getSimpleName();

    @Inject
    TrailerPlayerPresenter presenter;
    @Inject
    YouTubeManager youTubeManager;
    @Inject
    NetworkManager networkManager;
    @Inject
    PhoneManager phoneManager;

    private VideoPlayerFragment videoPlayerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MobileButterApplication.getAppContext()
                .getComponent()
                .trailerComponentBuilder()
                .trailerModule(new TrailerPlayerModule(this))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_videoplayer);

        final Media media = getIntent().getParcelableExtra(DATA);
        media.title += " " + getString(R.string.trailer);
        final String youtubeUrl = getIntent().getStringExtra(LOCATION);

        this.videoPlayerFragment = (VideoPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.video_fragment);

        presenter.onCreate(media, youtubeUrl);
    }

    public static Intent getIntent(Context context, Media media, String url) {
        Intent i = new Intent(context, TrailerPlayerActivity.class);
        i.putExtra(TrailerPlayerActivity.DATA, media);
        i.putExtra(TrailerPlayerActivity.LOCATION, url);
        return i;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        videoPlayerFragment.enableSubsButton(false);
    }

    @Override
    public void onExecuteQueryYoutubeTask(String youtubeUrl) {
        final QueryYouTubeTask youTubeTask = new QueryYouTubeTask(this, youTubeManager, networkManager, phoneManager);
        final String youTubeVideoId = youTubeManager.getYouTubeVideoId(youtubeUrl);
        youTubeTask.execute(youTubeVideoId);
    }

    @Override
    public void onNotifyMediaReady() {
        videoPlayerFragment.onMediaReady();
    }

    @Override
    public void onDisplayErrorVideoDialog() {
        try {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TrailerPlayerActivity.this)
                    .setTitle(R.string.comm_error)
                    .setCancelable(false)
                    .setMessage(R.string.comm_message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TrailerPlayerActivity.this.finish();
                        }
                    });
            final AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Problem showing error dialog: ", e);
        }
    }

    private static class QueryYouTubeTask extends AsyncTask<String, Void, Uri> {

        private final String TAG = QueryYouTubeTask.class.getSimpleName();

        private final YouTubeManager youTubeManager;
        private final NetworkManager networkManager;
        private final PhoneManager phoneManager;

        private final WeakReference<TrailerPlayerActivity> activityWeakReference;

        private QueryYouTubeTask(final TrailerPlayerActivity activity, final YouTubeManager youTubeManager,
                final NetworkManager networkManager,
                final PhoneManager phoneManager) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.youTubeManager = youTubeManager;
            this.networkManager = networkManager;
            this.phoneManager = phoneManager;
        }

        @Override
        protected Uri doInBackground(String... params) {
            final String videoId = params[0];
            try {
                int videoQuality;
                if (networkManager.isWifiConnected()) {
                    videoQuality = YouTubeManager.QUALITY_HIGH_MP4;
                } else if (phoneManager.isConnected() && phoneManager.isHighSpeedConnection()) {
                    videoQuality = YouTubeManager.QUALITY_NORMAL_MP4;
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
            final TrailerPlayerActivity trailerPlayerActivity = activityWeakReference.get();
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

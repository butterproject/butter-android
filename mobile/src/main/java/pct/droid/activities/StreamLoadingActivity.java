package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.base.PopcornApplication;
import pct.droid.base.RecursiveFileObserver;
import pct.droid.base.preferences.DefaultPlayer;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.providers.media.types.Movie;
import pct.droid.base.providers.media.types.Show;
import pct.droid.base.providers.subs.OpenSubsProvider;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.providers.subs.YSubsProvider;
import pct.droid.base.streamer.Status;
import pct.droid.base.utils.FileUtils;
import pct.droid.base.utils.LogUtils;
import pct.droid.base.utils.PrefUtils;

public class StreamLoadingActivity extends BaseActivity {

    public final static String STREAM_URL = "stream_url";
    public final static String DATA = "video_data";
    public final static String SHOW = "show";
    public final static String QUALITY = "quality";
    public final static String SUBTITLES = "subtitles";

    private FileObserver mFileObserver;
    private SubsProvider mSubsProvider;
    private Boolean mPlayerStarted = false, mHasSubs = false;

    private enum SubsStatus {SUCCESS, FAILURE, DOWNLOADING}

    ;
    private SubsStatus mSubsStatus = SubsStatus.DOWNLOADING;
    private String mSubtitleLanguage = null;

    @InjectView(R.id.progressIndicator)
    ProgressBar progressIndicator;
    @InjectView(R.id.progressText)
    TextView progressText;
    @InjectView(R.id.downloadSpeedText)
    TextView downloadSpeedText;
    @InjectView(R.id.seedsText)
    TextView seedsText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_streamloading);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        while (!getApp().isServiceBound()) {
            getApp().startService();
        }

        if (!getIntent().hasExtra(STREAM_URL) && !getIntent().hasExtra(DATA)) {
            finish();
        }

        String streamUrl = getIntent().getStringExtra(STREAM_URL);
        final Media data = getIntent().getParcelableExtra(DATA);

        if (!getIntent().hasExtra(SUBTITLES) && data.subtitles != null && data.subtitles.size() > 0) {
            if (data.subtitles.containsKey(PrefUtils.get(this, Prefs.SUBTITLE_DEFAULT, "no-subs"))) {
                getIntent().putExtra(SUBTITLES, PrefUtils.get(this, Prefs.SUBTITLE_DEFAULT, "no-subs"));
            }
        }

        if (data.subtitles != null && data.subtitles.size() > 0) {
            if (getIntent().hasExtra(SUBTITLES)) {
                mHasSubs = true;
                mSubtitleLanguage = getIntent().getStringExtra(SUBTITLES);
                if (!mSubtitleLanguage.equals("no-subs")) {
                    SubsProvider.download(this, data, mSubtitleLanguage, new Callback() {
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
                mSubsStatus = SubsStatus.SUCCESS;
            }
        } else {
            // TODO: make more generic
            if (data instanceof Movie) {
                mSubsProvider = new YSubsProvider();
                mSubsProvider.getList((Movie) data, new SubsProvider.Callback() {
                    @Override
                    public void onSuccess(Map<String, String> items) {
                        data.subtitles = items;
                        mSubsStatus = SubsStatus.SUCCESS;
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mSubsStatus = SubsStatus.FAILURE;
                    }
                });
            } else {
                mSubsProvider = new OpenSubsProvider();
                Show.Episode episode = (Show.Episode) data;
                Show show = getIntent().getParcelableExtra(SHOW);
                mSubsProvider.getList(show, episode, new SubsProvider.Callback() {
                    @Override
                    public void onSuccess(Map<String, String> items) {
                        data.subtitles = items;
                        mSubsStatus = SubsStatus.SUCCESS;
                    }

                    @Override
                    public void onFailure(Exception e) {
                        mSubsStatus = SubsStatus.FAILURE;
                    }
                });
            }
        }

        getApp().startStreamer(streamUrl);

        String directory = PrefUtils.get(this, Prefs.STORAGE_LOCATION, PopcornApplication.getStreamDir()) + "/";
        mFileObserver = new RecursiveFileObserver(directory) {
            @Override
            public void onEvent(int event, String path) {
                if (path == null) return;
                if (path.contains("streamer.json")) {
                    switch (event) {
                        case CREATE:
                            break;
                        case MODIFY:
                            startPlayer();
                            break;
                    }
                } else if (path.contains("status.json")) {
                    switch (event) {
                        case CREATE:
                        case MODIFY:
                            updateStatus();
                            break;
                    }
                }
            }
        };

        mFileObserver.startWatching();
    }

    private void startPlayer() {
        if (mHasSubs && mSubsStatus == SubsStatus.DOWNLOADING) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressText.setText(R.string.waiting_for_subtitles);
                }
            });
            return;
        }

        if (!mPlayerStarted && progressIndicator.getProgress() == progressIndicator.getMax()) {
            try {
                Status status = Status.parseJSON(FileUtils.getContentsAsString(PrefUtils.get(this, Prefs.STORAGE_LOCATION, PopcornApplication.getStreamDir()) + "/status.json"));
                mPlayerStarted = true;
                String location = status.filePath;
                if (!DefaultPlayer.start(this, (Media) getIntent().getParcelableExtra(DATA), mSubtitleLanguage, location)) {
                    Intent i = new Intent(StreamLoadingActivity.this, VideoPlayerActivity.class);
                    if (getIntent().hasExtra(DATA)) {
                        i.putExtra(VideoPlayerActivity.DATA, getIntent().getParcelableExtra(DATA));
                    }
                    if (getIntent().hasExtra(QUALITY)) {
                        i.putExtra(VideoPlayerActivity.QUALITY, getIntent().getStringExtra(QUALITY));
                    }
                    if (mSubsStatus == SubsStatus.SUCCESS) {
                        i.putExtra(VideoPlayerActivity.SUBTITLES, getIntent().getStringExtra(SUBTITLES));
                    }
                    i.putExtra(VideoPlayerActivity.LOCATION, "file://" + location);
                    startActivity(i);
                    finish();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateStatus() {
        try {
            final DecimalFormat df = new DecimalFormat("#############0.00");
            final Status status = Status.parseJSON(FileUtils.getContentsAsString(PrefUtils.get(this, Prefs.STORAGE_LOCATION, PopcornApplication.getStreamDir()) + "/status.json"));
            if (status == null) return;
            LogUtils.d(status.toString());
            int calculateProgress = (int) Math.floor(status.progress * 25);
            if (calculateProgress > 100) calculateProgress = 100;
            final int progress = calculateProgress;
            if (progressIndicator.getProgress() < 100) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressIndicator.setIndeterminate(false);
                        progressIndicator.setProgress(progress);
                        progressText.setText(progress + "%");

                        if (status.downloadSpeed < 1048576) {
                            downloadSpeedText.setText(df.format(status.downloadSpeed / 1024) + " KB/s");
                        } else {
                            downloadSpeedText.setText(df.format(status.downloadSpeed / 1048576) + " MB/s");
                        }
                        seedsText.setText(status.seeds + " " + getString(R.string.seeds));
                    }
                });
            } else {
                startPlayer();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSubsStatus == SubsStatus.DOWNLOADING) {
                            progressText.setText(R.string.waiting_for_subtitles);
                        } else {
                            progressText.setText(R.string.streaming_started);
                        }


                        downloadSpeedText.setText(df.format((status.downloadSpeed / 1048576)) + " MB/s");
                        seedsText.setText(status.seeds + " " + getString(R.string.seeds));
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFileObserver.startWatching();
        if (mPlayerStarted) {
            onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFileObserver.stopWatching();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getApp().stopStreamer();
    }
}
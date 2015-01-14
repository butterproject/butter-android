package pct.droid.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.base.preferences.DefaultPlayer;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.providers.media.types.Movie;
import pct.droid.base.providers.media.types.Show;
import pct.droid.base.providers.subs.OpenSubsProvider;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.providers.subs.YSubsProvider;
import pct.droid.base.streamer.StreamerService;
import pct.droid.base.streamer.StreamerStatus;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.ThreadUtils;

public class StreamLoadingActivity extends BaseActivity implements StreamerService.Listener {

    public final static String STREAM_URL = "stream_url";
    public final static String DATA = "video_data";
    public final static String SHOW = "show";
    public final static String QUALITY = "quality";
    public final static String SUBTITLES = "subtitles";

    private SubsProvider mSubsProvider;
    private Boolean mPlayerStarted = false, mHasSubs = false;
    private StreamerService mService;

    private enum SubsStatus {SUCCESS, FAILURE, DOWNLOADING};
    private SubsStatus mSubsStatus = SubsStatus.DOWNLOADING;
    private String mSubtitleLanguage = null, mVideoLocation = "";

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

        if (!getIntent().hasExtra(STREAM_URL) && !getIntent().hasExtra(DATA)) {
            finish();
        }

        final Media data = getIntent().getParcelableExtra(DATA);

        if(null != data) {
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
        }
    }

    private void startPlayer(String location) {
        if (mHasSubs && mSubsStatus == SubsStatus.DOWNLOADING) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    progressText.setText(R.string.waiting_for_subtitles);
                }
            });
            return;
        }

        if (!mPlayerStarted) {
            mPlayerStarted = true;
            if (!DefaultPlayer.start(this, (Media) getIntent().getParcelableExtra(DATA), mSubtitleLanguage, location)) {
                mService.removeListener();
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
        }
    }

    private void updateStatus(final StreamerStatus status) {
        final DecimalFormat df = new DecimalFormat("#############0.00");
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressIndicator.setIndeterminate(false);
                progressIndicator.setProgress(status.bufferProgress);
                progressText.setText(status.bufferProgress + "%");

                if (status.downloadSpeed / 1024 < 1000) {
                    downloadSpeedText.setText(df.format(status.downloadSpeed / 1024) + " KB/s");
                } else {
                    downloadSpeedText.setText(df.format(status.downloadSpeed / 1048576) + " MB/s");
                }
                seedsText.setText(status.seeds + " " + getString(R.string.seeds));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        StreamerService.bindHere(this, mServiceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayerStarted) {
            onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mService != null)
            unbindService(mServiceConnection);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mService != null) {
            mService.stopStreaming();
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((StreamerService.ServiceBinder) service).getService();
            mService.setListener(StreamLoadingActivity.this);
            mService.streamTorrent(getIntent().getStringExtra(STREAM_URL));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    public void onStreamStarted() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressText.setText(R.string.buffering_started);
            }
        });
    }

    @Override
    public void onStreamError(Exception e) {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamLoadingActivity.this, R.string.error_files, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onStreamReady(File videoLocation) {
        mVideoLocation = videoLocation.toString();
        startPlayer(mVideoLocation);
    }

    @Override
    public void onStreamProgress(StreamerStatus status) {
        if(mVideoLocation.isEmpty()) {
            updateStatus(status);
        } else {
            startPlayer(mVideoLocation);
        }
    }

}
package pct.droid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.InjectView;
import pct.droid.R;
import pct.droid.streamer.Status;
import pct.droid.utils.FileUtils;
import pct.droid.utils.LogUtils;

public class StreamLoadingActivity extends BaseActivity {

    public final static String STREAM_URL = "stream_url";
    public final static String DATA = "video_data";
    public final static String QUALITY = "quality";

    private FileObserver mFileObserver;
    private Boolean mIntentStarted = false;

    @InjectView(R.id.progressIndicator)
    ProgressBar progressIndicator;
    @InjectView(R.id.progressText)
    TextView progressText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_streamloading);

        if(!getIntent().hasExtra(STREAM_URL) && !getIntent().hasExtra(DATA)) {
            finish();
        }

        String streamUrl = getIntent().getStringExtra(STREAM_URL);

        while(!getApp().isServiceBound()) {
            getApp().startService();
        }

        getApp().startStreamer(streamUrl);

        String directory = getApp().getStreamDir();
        mFileObserver = new FileObserver(directory) {
            @Override
            public void onEvent(int event, String path) {
                if(path == null) return;
                if(path.contains("streamer.json")) {
                    switch (event) {
                        case CREATE:
                            LogUtils.d("Streamer file created");
                            break;
                        case MODIFY:
                            LogUtils.d("Streamer file modified");
                            startPlayer();
                            break;
                    }
                } else if(path.contains("status.json")) {
                    switch (event) {
                        case CREATE:
                        case MODIFY:
                            LogUtils.d("Status file changed");
                            updateStatus();
                            break;
                    }
                }
            }
        };

        mFileObserver.startWatching();
    }

    private void startPlayer() {
        if(!mIntentStarted && progressIndicator.getProgress() == progressIndicator.getMax()) {
            mIntentStarted = true;
            Intent i = new Intent(StreamLoadingActivity.this, VideoPlayerActivity.class);
            i.putExtra(VideoPlayerActivity.DATA, getIntent().getParcelableExtra(DATA));
            i.putExtra(VideoPlayerActivity.LOCATION, "http://localhost:9999");
            i.putExtra(VideoPlayerActivity.QUALITY, getIntent().getStringExtra(QUALITY));
            startActivity(i);
            finish();
        }
    }

    private void updateStatus() {
        try {
            final Status status = Status.parseJSON(FileUtils.getContentsAsString(getApp().getStreamDir() + "/status.json"));
            if(status == null) return;
            LogUtils.d(status.toString());
            int calculateProgress = (int)Math.floor(status.progress * 25);
            if(calculateProgress > 100) calculateProgress = 100;
            final int progress = calculateProgress;
            if(progressIndicator.getProgress() < 100) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressIndicator.setIndeterminate(false);
                        progressIndicator.setProgress(progress);
                        progressText.setText(progress + "%");
                    }
                });
            } else {
                startPlayer();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressText.setText("Streaming"); // TODO: translation (by sv244)
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

package pct.droid.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;

import pct.droid.R;
import pct.droid.utils.LogUtils;

public class StreamLoadingActivity extends BaseActivity {

    private FileObserver mFileObserver;
    private Boolean mStreaming = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_streamloading);

        if(!getIntent().hasExtra("stream_url")) {
            finish();
        }

        String streamUrl = getIntent().getStringExtra("stream_url");
        getApp().startStreamer(streamUrl);

        String directory = getApp().getStreamDir();
        mFileObserver = new FileObserver(directory) {
            @Override
            public void onEvent(int event, String path) {
                if(path == null) return;
                if(path.contains("streamer.json")) {
                    switch (event) {
                        case CREATE:
                            LogUtils.d("StreamLoadingActivity", "Streamer file created");
                            break;
                        case MODIFY:
                            LogUtils.d("StreamLoadingActivity", "Streamer file modified");
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setDataAndType(Uri.parse("http://localhost:9999"), "video/*");
                            startActivity(i);
                            break;
                    }
                } else if(path.contains("status.json")) {
                    switch (event) {
                        case CREATE:
                        case MODIFY:
                            LogUtils.d("StreamLoadingActivity", "Status file changed");
                            break;
                    }
                }
            }
        };

        mFileObserver.startWatching();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mStreaming) {
            getApp().stopStreamer();
            finish();
        } else {
            mStreaming = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFileObserver.stopWatching();
    }
}

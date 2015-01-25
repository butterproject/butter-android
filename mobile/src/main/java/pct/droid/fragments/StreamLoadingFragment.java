package pct.droid.fragments;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.activities.VideoPlayerActivity;
import pct.droid.base.fragments.BaseStreamLoadingFragment;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.torrent.DownloadStatus;
import pct.droid.base.utils.ThreadUtils;
import pct.droid.base.utils.VersionUtils;

public class StreamLoadingFragment extends BaseStreamLoadingFragment {

    View mRoot;
    @InjectView(R.id.progress_indicator)
    ProgressBar progressIndicator;
    @InjectView(R.id.primary_textview)
    TextView mPrimaryTextView;
    @InjectView(R.id.secondary_textview)
    TextView mSecondaryTextView;
    @InjectView(R.id.tertiary_textview)
    TextView mTertiaryTextView;
    @InjectView(R.id.background_imageview)
    ImageView mBackgroundImageView;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_streamloading, container, false);
        ButterKnife.inject(this, mRoot);

        if (VersionUtils.isLollipop()) {
            //postpone the transitions until after the view is layed out.
            getActivity().postponeEnterTransition();

            mRoot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    mRoot.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }

        return mRoot;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadBackgroundImage();
    }

    private void loadBackgroundImage() {
        StreamInfo info = mCallback.getStreamInformation();
          /* attempt to load background image */
        if (null != info) {
            String url;
            if (info.isShow()) url = info.getShow().image;
            else url = info.getMedia().image;

            if (!TextUtils.isEmpty(url))
                Picasso.with(getActivity()).load(url).error(R.color.bg).into(mBackgroundImageView);
        }
    }

    private void updateStatus(final DownloadStatus status) {
        final DecimalFormat df = new DecimalFormat("#############0.00");
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressIndicator.setIndeterminate(false);
                progressIndicator.setProgress(status.bufferProgress);
                mPrimaryTextView.setText(status.bufferProgress + "%");

                if (status.downloadSpeed / 1024 < 1000) {
                    mSecondaryTextView.setText(df.format(status.downloadSpeed / 1024) + " KB/s");
                } else {
                    mSecondaryTextView.setText(df.format(status.downloadSpeed / 1048576) + " MB/s");
                }
                mTertiaryTextView.setText(status.seeds + " " + getString(R.string.seeds));
            }
        });
    }


    @Override
    protected void updateView(State state, Object extra) {

        switch (state) {
            case UNINITIALISED:
                mTertiaryTextView.setText(null);
                mPrimaryTextView.setText(null);
                mSecondaryTextView.setText(null);
                progressIndicator.setIndeterminate(true);
                progressIndicator.setProgress(0);
                break;
            case ERROR:
                if (null != extra && extra instanceof String)
                    mPrimaryTextView.setText((String) extra);
                mSecondaryTextView.setText(null);
                mTertiaryTextView.setText(null);
                progressIndicator.setIndeterminate(true);
                progressIndicator.setProgress(0);
                break;
            case BUFFERING:
                mPrimaryTextView.setText(R.string.starting_buffering);
                mTertiaryTextView.setText(null);
                mSecondaryTextView.setText(null);
                progressIndicator.setIndeterminate(true);
                progressIndicator.setProgress(0);
                break;
            case STREAMING:
                mPrimaryTextView.setText(R.string.streaming_started);
                if (null != extra && extra instanceof DownloadStatus)
                    updateStatus((DownloadStatus) extra);
                break;
            case WAITING_SUBTITLES:
                mPrimaryTextView.setText(R.string.waiting_for_subtitles);
                mTertiaryTextView.setText(null);
                mSecondaryTextView.setText(null);
                progressIndicator.setIndeterminate(true);
                progressIndicator.setProgress(0);
                break;
            case WAITING_TORRENT:
                mPrimaryTextView.setText(R.string.waiting_torrent);
                mTertiaryTextView.setText(null);
                mSecondaryTextView.setText(null);
                progressIndicator.setIndeterminate(true);
                progressIndicator.setProgress(0);
                break;

        }
    }

    @Override
    protected void startPlayerActivity(FragmentActivity activity, String location, Media media, String quality,
                                       String subtitleLanguage,
                                       int resumePosition) {
        VideoPlayerActivity.startActivity(activity, location, media, quality, subtitleLanguage, resumePosition);
    }
}

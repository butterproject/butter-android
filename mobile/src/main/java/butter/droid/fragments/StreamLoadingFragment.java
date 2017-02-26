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

package butter.droid.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.sv244.torrentstream.StreamStatus;
import com.github.sv244.torrentstream.Torrent;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import javax.inject.Inject;

import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.activities.BeamPlayerActivity;
import butter.droid.activities.VideoPlayerActivity;
import butter.droid.base.ui.loading.fragment.BaseStreamLoadingFragment;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.manager.beaming.BeamManager;
import butter.droid.base.manager.vlc.PlayerManager;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.utils.FragmentUtil;
import butter.droid.base.utils.PixelUtils;
import butter.droid.base.utils.ThreadUtils;
import butter.droid.base.utils.VersionUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hugo.weaving.DebugLog;

public class StreamLoadingFragment extends BaseStreamLoadingFragment {

    private Context mContext;
    private Torrent mCurrentTorrent;

    @Inject BeamManager beamManager;
    @Inject PlayerManager playerManager;

    @BindView(R.id.progress_indicator) ProgressBar mProgressIndicator;
    @BindView(R.id.primary_textview) TextView mPrimaryTextView;
    @BindView(R.id.secondary_textview) TextView mSecondaryTextView;
    @BindView(R.id.tertiary_textview) TextView mTertiaryTextView;
    @BindView(R.id.background_imageview) ImageView mBackgroundImageView;
    @BindView(R.id.startexternal_button) Button mStartExternalButton;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileButterApplication.getAppContext()
                .getComponent()
                .inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_streamloading, container, false);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    @Override public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        if (VersionUtils.isLollipop()) {
            //postpone the transitions until after the view is layed out.
            getActivity().postponeEnterTransition();

            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayingExternal) {
            setState(State.STREAMING);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = getActivity();
        loadBackgroundImage();
    }

    @Override
    public void onStreamPrepared(Torrent torrent) {
        mCurrentTorrent = torrent;

        if (TextUtils.isEmpty(this.streamInfo.getTitle())) {
            StringArraySelectorDialogFragment.show(getChildFragmentManager(), R.string.select_file,
                    mCurrentTorrent.getFileNames(), -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            mCurrentTorrent.setSelectedFile(position);
                            StreamLoadingFragment.super.onStreamPrepared(mCurrentTorrent);
                        }
                    });
            return;
        }

        super.onStreamPrepared(mCurrentTorrent);
    }

    private void loadBackgroundImage() {
        if (null != streamInfo) {
            String url = streamInfo.getImageUrl();
            if (PixelUtils.isTablet(mContext)) {
                url = streamInfo.getHeaderImageUrl();
            }

            if (!TextUtils.isEmpty(url)) {
                Picasso.with(mContext).load(url).error(R.color.bg).into(mBackgroundImageView);
            }
        }
    }

    private void updateStatus(final StreamStatus status) {
        if (!FragmentUtil.isAdded(this)) {
            return;
        }

        final DecimalFormat df = new DecimalFormat("#############0.00");
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressIndicator.setIndeterminate(false);
                if (!mPlayingExternal) {
                    mProgressIndicator.setProgress(status.bufferProgress);
                    mPrimaryTextView.setText(status.bufferProgress + "%");
                } else {
                    int progress = ((Float) status.progress).intValue();
                    mProgressIndicator.setProgress(progress);
                    mPrimaryTextView.setText(progress + "%");
                }

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
                mProgressIndicator.setIndeterminate(true);
                mProgressIndicator.setProgress(0);
                break;
            case ERROR:
                if (null != extra && extra instanceof String) {
                    mPrimaryTextView.setText((String) extra);
                }
                mSecondaryTextView.setText(null);
                mTertiaryTextView.setText(null);
                mProgressIndicator.setIndeterminate(true);
                mProgressIndicator.setProgress(0);
                break;
            case BUFFERING:
                mPrimaryTextView.setText(R.string.starting_buffering);
                mTertiaryTextView.setText(null);
                mSecondaryTextView.setText(null);
                mProgressIndicator.setIndeterminate(true);
                mProgressIndicator.setProgress(0);
                break;
            case STREAMING:
                if (null != extra && extra instanceof StreamStatus) {
                    updateStatus((StreamStatus) extra);
                }
                break;
            case WAITING_SUBTITLES:
                mPrimaryTextView.setText(R.string.waiting_for_subtitles);
                mTertiaryTextView.setText(null);
                mSecondaryTextView.setText(null);
                mProgressIndicator.setIndeterminate(true);
                mProgressIndicator.setProgress(0);
                break;
            case WAITING_TORRENT:
                mPrimaryTextView.setText(R.string.waiting_torrent);
                mTertiaryTextView.setText(null);
                mSecondaryTextView.setText(null);
                mProgressIndicator.setIndeterminate(true);
                mProgressIndicator.setProgress(0);
                break;

        }
    }

    @Override
    @DebugLog
    protected void startPlayerActivity(String location, int resumePosition) {
        if (FragmentUtil.isAdded(this) && !mPlayerStarted) {
            this.streamInfo.setVideoLocation(location);
            if (beamManager.isConnected()) {
                BeamPlayerActivity.startActivity(mContext, this.streamInfo, resumePosition);
            } else {
                mPlayingExternal = playerManager.start(this.streamInfo.getMedia(), this.streamInfo.getSubtitleLanguage(),
                        location);
                if (!mPlayingExternal) {
                    VideoPlayerActivity.startActivity(mContext, this.streamInfo, resumePosition);
                }
            }

            if (!mPlayingExternal) {
                getActivity().finish();
            } else {
                mStartExternalButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick(R.id.startexternal_button)
    public void externalClick(View v) {
        playerManager.start(this.streamInfo.getMedia(), this.streamInfo.getSubtitleLanguage(), this.streamInfo.getVideoLocation());
    }

    public static StreamLoadingFragment newInstance(@NonNull StreamInfo streamInfo) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_STREAM_INFO, streamInfo);

        StreamLoadingFragment fragment = new StreamLoadingFragment();
        fragment.setArguments(args);

        return fragment;
    }
}

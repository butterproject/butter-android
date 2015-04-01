/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.command.ServiceCommandError;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pct.droid.R;
import pct.droid.activities.BaseActivity;
import pct.droid.base.connectsdk.BeamManager;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.torrent.StreamInfo;
import pct.droid.base.torrent.TorrentService;
import pct.droid.base.utils.AnimUtils;
import pct.droid.dialogfragments.LoadingBeamingDialogFragment;
import pct.droid.dialogfragments.OptionDialogFragment;
import timber.log.Timber;

public class BeamPlayerFragment extends Fragment {

    private StreamInfo mStreamInfo;
    private Media mMedia;
    private BaseActivity mActivity;
    private BeamManager mBeamManager = BeamManager.getInstance(getActivity());
    private MediaControl mMediaControl;
    private VolumeControl mVolumeControl;
    private TorrentService mService;
    private boolean mHasVolumeControl = false, mIsPlaying = false;
    private int mRetries = 0;
    private LoadingBeamingDialogFragment mLoadingDialog;

    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    @InjectView(R.id.play_button)
    ImageButton mPlayButton;
    @InjectView(R.id.cover_image)
    ImageView mCoverImage;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_beamplayer, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (BaseActivity) getActivity();
        mActivity.setSupportActionBar(mToolbar);

        mLoadingDialog = LoadingBeamingDialogFragment.newInstance();
        mLoadingDialog.show(getChildFragmentManager(), "overlay_fragment");

        mStreamInfo = ((VideoPlayerFragment.Callback) mActivity).getInfo();
        if(mStreamInfo.isShow()) {
            mMedia = mStreamInfo.getShow();
        } else {
            mMedia = mStreamInfo.getMedia();
        }

        TorrentService.bindHere(getActivity(), mServiceConnection);

        if (mMedia.image != null && !mMedia.image.equals("")) {
            Picasso.with(mCoverImage.getContext()).load(mMedia.image)
                    .into(mCoverImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            AnimUtils.fadeIn(mCoverImage);
                        }

                        @Override
                        public void onError() {}
                    });
        }

        mActivity.getSupportActionBar().setTitle(getString(R.string.now_playing));
        mActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_clear_mtrl_alpha);

        startVideo();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mService != null) {
            mActivity.unbindService(mServiceConnection);
            mService.stopStreaming();
        }
    }

    private void startVideo() {
        mBeamManager.playVideo(mStreamInfo, false, new MediaPlayer.LaunchListener() {
            @Override
            public void onSuccess(MediaPlayer.MediaLaunchObject object) {
                mMediaControl = object.mediaControl;

                mMediaControl.subscribePlayState(mPlayStateListener);
                mMediaControl.getPlayState(mPlayStateListener);

                mVolumeControl = BeamManager.getInstance(getActivity()).getVolumeControl();
                if(mVolumeControl != null) mHasVolumeControl = true;
            }

            @Override
            public void onError(ServiceCommandError error) {
                Timber.e(error.getCause(), error.getMessage());
                if(mRetries > 2) {
                    if(mLoadingDialog.isVisible()) {
                        mLoadingDialog.dismiss();
                    }

                    OptionDialogFragment.show(mActivity, getChildFragmentManager(), R.string.unknown_error, R.string.beaming_failed, android.R.string.yes, android.R.string.no, new OptionDialogFragment.Listener() {
                        @Override
                        public void onSelectionPositive() {
                            startVideo();
                        }

                        @Override
                        public void onSelectionNegative() {
                            getActivity().finish();
                        }
                    });

                    return;
                }

                startVideo();
                mRetries++;
            }
        });
    }

    @OnClick(R.id.play_button)
    public void playPauseClick(View v) {
        if(mIsPlaying) {
            mIsPlaying = false;
            mMediaControl.pause(null);
        } else {
            mIsPlaying = true;
            mMediaControl.play(null);
        }
        mMediaControl.getPlayState(mPlayStateListener);
    }

    private MediaControl.PlayStateListener mPlayStateListener = new MediaControl.PlayStateListener() {
        @Override
        public void onSuccess(MediaControl.PlayStateStatus state) {
            mIsPlaying = state.equals(MediaControl.PlayStateStatus.Playing);
            mPlayButton.setImageResource(mIsPlaying ? R.drawable.ic_av_pause : R.drawable.ic_av_play);

            if(mLoadingDialog.isVisible() && mIsPlaying) {
                mLoadingDialog.dismiss();
            }
        }

        @Override
        public void onError(ServiceCommandError error) {
            if(mLoadingDialog.isVisible() && error.getCode() == 500) {
                mLoadingDialog.dismiss();

                OptionDialogFragment.show(mActivity, getChildFragmentManager(), R.string.unknown_error, R.string.beaming_failed, android.R.string.yes, android.R.string.no, new OptionDialogFragment.Listener() {
                    @Override
                    public void onSelectionPositive() {
                        startVideo();
                    }

                    @Override
                    public void onSelectionNegative() {
                        getActivity().finish();
                    }
                });
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((TorrentService.ServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
}

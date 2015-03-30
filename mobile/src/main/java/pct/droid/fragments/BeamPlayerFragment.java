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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaPlayer;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.command.ServiceCommandError;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import pct.droid.R;
import pct.droid.base.connectsdk.BeamManager;
import pct.droid.base.connectsdk.server.BeamServer;
import pct.droid.base.torrent.TorrentService;
import timber.log.Timber;

public class BeamPlayerFragment extends Fragment {

    private VideoPlayerFragment.Callback mCallback;
    private Activity mActivity;
    private BeamManager mBeamManager = BeamManager.getInstance(getActivity());
    private MediaControl mMediaControl;
    private VolumeControl mVolumeControl;
    private TorrentService mService;
    private boolean mHasVolumeControl = false, mIsPlaying = false;
    private int mRetries = 0;

    @InjectView(R.id.play_button)
    Button mPlayButton;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_beamplayer, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
        mCallback = (VideoPlayerFragment.Callback) mActivity;

        TorrentService.bindHere(getActivity(), mServiceConnection);

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
        mBeamManager.playVideo(mCallback.getData(), mCallback.getLocation(), false, new MediaPlayer.LaunchListener() {
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
                if(mRetries > 2) return;

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
        mPlayButton.setText(mIsPlaying ? "Pause" : "Play");
        mMediaControl.getPlayState(mPlayStateListener);
    }

    private MediaControl.PlayStateListener mPlayStateListener = new MediaControl.PlayStateListener() {
        @Override
        public void onSuccess(MediaControl.PlayStateStatus state) {
            mIsPlaying = state.equals(MediaControl.PlayStateStatus.Playing);
            mPlayButton.setText(mIsPlaying ? "Pause" : "Play");
        }

        @Override
        public void onError(ServiceCommandError error) { }
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

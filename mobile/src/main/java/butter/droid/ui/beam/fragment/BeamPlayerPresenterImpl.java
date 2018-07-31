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

package butter.droid.ui.beam.fragment;

import android.annotation.SuppressLint;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.MediaControl;
import com.connectsdk.service.capability.MediaControl.PlayStateListener;
import com.connectsdk.service.capability.MediaControl.PositionListener;
import com.connectsdk.service.capability.MediaPlayer.LaunchListener;
import com.connectsdk.service.capability.MediaPlayer.MediaLaunchObject;
import com.connectsdk.service.capability.VolumeControl;
import com.connectsdk.service.capability.VolumeControl.VolumeListener;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import butter.droid.R;
import butter.droid.base.manager.internal.beaming.BeamDeviceListener;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.providers.media.model.StreamInfo;
import butter.droid.base.utils.StringUtils;
import butter.droid.ui.beam.BeamPlayerActivityPresenter;
import timber.log.Timber;

public class BeamPlayerPresenterImpl implements BeamPlayerPresenter, LaunchListener {

    private static final int REFRESH_INTERVAL_MS = (int) TimeUnit.SECONDS.toMillis(1);
    private static final int BUTTON_SEEK_OFFSET = 10000;

    private final BeamPlayerView view;
    private final BeamManager beamManager;
    private final BeamPlayerActivityPresenter parentPresenter;

    private StreamInfo streamInfo;
    private long resumePosition;
    private long totalTimeDuration = 0;

    private boolean hasVolumeControl = true;
    private boolean hasSeekControl = true;
    private boolean isPlaying = false;
    private boolean processingSeeking = false;

    private int beamRetries = 0;
    private MediaControl mediaControl;
    @Nullable private VolumeControl volumeControl;

    @Nullable private ServiceSubscription<PlayStateListener> playStateListenerSubscription;
    @Nullable private ServiceSubscription<VolumeListener> volumeListenerSubscription;

    private ScheduledFuture task;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    public BeamPlayerPresenterImpl(final BeamPlayerView view, final BeamManager beamManager,
            final BeamPlayerActivityPresenter parentPresenter) {
        this.view = view;
        this.beamManager = beamManager;
        this.parentPresenter = parentPresenter;
    }


    @Override public void onCreate(final StreamInfo streamInfo, final long resumePosition) {

        if (streamInfo == null) {
            throw new IllegalStateException("Stream info not provided");
        }

        this.streamInfo = streamInfo;
        this.resumePosition = resumePosition;

        view.startNotificationService(isPlaying);
    }

    @Override public void onViewCreated() {
        prepareUi(streamInfo);
    }

    @Override public void onResume() {
        beamManager.addDeviceListener(deviceListener);
    }

    @Override public void onPause() {
        beamManager.removeDeviceListener(deviceListener);
    }

    @Override public void playPauseClicked() {
        if (mediaControl == null) {
            return;
        }

        ResponseListener<Object> responseListener = new ResponseListener<Object>() {
            @Override
            public void onSuccess(Object object) {
                // nothing to do, we have play state listener
            }

            @Override
            public void onError(ServiceCommandError error) {
                // nothing to do, we have play state listener
            }
        };

        if (isPlaying) {
            mediaControl.pause(responseListener);
        } else {
            mediaControl.play(responseListener);
        }

    }

    @Override public void forwardClicked() {
        if (mediaControl == null) {
            return;
        }

        mediaControl.getPosition(new PositionListener() {
            @Override public void onSuccess(final Long object) {
                long newProgress = object + BUTTON_SEEK_OFFSET;
                if (newProgress > totalTimeDuration) {
                    newProgress = totalTimeDuration;
                }

                mediaControl.seek(newProgress, null);
            }

            @Override public void onError(final ServiceCommandError error) {
                Timber.d("position error");
            }
        });
    }

    @Override public void backwardClicked() {
        if (mediaControl == null) {
            return;
        }

        mediaControl.getPosition(new PositionListener() {
            @Override public void onSuccess(final Long object) {
                long newProgress = object - BUTTON_SEEK_OFFSET;
                if (newProgress < 0) {
                    newProgress = 0;
                }
                mediaControl.seek(newProgress, null);
            }

            @Override public void onError(final ServiceCommandError error) {
                Timber.d("Position error");
            }
        });
    }

    @Override public void closePlayer() {
        beamManager.stopVideo();
        parentPresenter.closePlayer();
    }

    @Override public void onUserVolumeChanged(final int progress) {
        if (volumeControl != null) {
            volumeControl.setVolume((float) progress / 100.0f, null);
        }
    }

    @Override public void seek(final int progress) {
        if (!processingSeeking) {
            processingSeeking = true;
            mediaControl.seek(progress, new ResponseListener<Object>() {
                @Override
                public void onSuccess(Object response) {
                    processingSeeking = false;
                }

                @Override
                public void onError(ServiceCommandError error) {
                    processingSeeking = false;
                }
            });
        }
    }

    @Override public void beamVideo() {
        beamVideo(streamInfo);
    }

    private void beamVideo(StreamInfo streamInfo) {
        beamManager.playVideo(streamInfo, this);
    }

    private void prepareUi(StreamInfo streamInfo) {
        Integer paletteColor = streamInfo.getPaletteColor();

        view.tintProgress(paletteColor);

        String imageUrl = streamInfo.getBackdropImage();
        if (!StringUtils.isEmpty(imageUrl)) {
            view.loadCoverImage(imageUrl);
        }

        prepareBeamUi();
        beamVideo(streamInfo);
    }

    private void prepareBeamUi() {

        try {
            ConnectableDevice connectedDevice = beamManager.getConnectedDevice();
            if (!connectedDevice.hasCapability(MediaControl.Position)
                    || !connectedDevice.hasCapability(MediaControl.Seek)
                    || !connectedDevice.hasCapability(MediaControl.Duration)) {
                hasSeekControl = false;
                view.hideSeekBar();
            }

            if (!connectedDevice.hasCapability(VolumeControl.Volume_Get)
                    || !connectedDevice.hasCapability(VolumeControl.Volume_Set)
                    || !connectedDevice.hasCapability(VolumeControl.Volume_Subscribe)) {
                hasVolumeControl = false;
                view.disableVolumePanel();
            }

            if (!connectedDevice.hasCapability(MediaControl.Pause)) {
                view.disablePlayButton();
            }
        } catch (Exception e) { // TODO: 3/25/17 This should be handled properly
            view.showErrorMessage(R.string.unknown_error);
            view.closeScreen();
        }

    }

    @Override public void onSuccess(final MediaLaunchObject mediaLaunchObject) {
        mediaControl = mediaLaunchObject.mediaControl;

        playStateListenerSubscription = mediaControl.subscribePlayState(playStateListener);
        mediaControl.getPlayState(playStateListener);

        if (hasVolumeControl) {
            volumeControl = beamManager.getVolumeControl();
            volumeListenerSubscription = volumeControl.subscribeVolume(volumeListener);
            volumeControl.getVolume(volumeListener);
        }

        if (hasSeekControl) {
            startUpdating();
            mediaControl.getDuration(durationListener);
        }

        if (resumePosition > 0) {
            mediaControl.seek(resumePosition, null);
        }

    }

    @SuppressLint("TimberExceptionLogging") @Override public void onError(final ServiceCommandError error) {
        Timber.e(error.getCause(), error.getMessage());
        if (beamRetries > 2) {

            view.hideLoadingDialog();

            view.showBeamFailedDialog();
        } else {
            beamVideo(streamInfo);
            beamRetries++;
        }
    }

    private void unsubscribe() {
        if (playStateListenerSubscription != null) {
            playStateListenerSubscription.unsubscribe();
            playStateListenerSubscription = null;
        }

        if (volumeListenerSubscription != null) {
            volumeListenerSubscription.unsubscribe();
            volumeListenerSubscription = null;
        }
    }

    private void startUpdating() {
        if (task == null) {
            task = executor.scheduleAtFixedRate(positionRunnable, 0, REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void stopUpdating() {
        if (task != null) {
            for (Runnable r : executor.getQueue()) {
                executor.remove(r);
            }
            task.cancel(false);
            task = null;
        }
    }


    private final MediaControl.PlayStateListener playStateListener = new MediaControl.PlayStateListener() {
        @Override
        public void onSuccess(MediaControl.PlayStateStatus state) {
            isPlaying = state.equals(MediaControl.PlayStateStatus.Playing);
            view.updatePlayButton(
                    isPlaying ? R.drawable.ic_av_pause : R.drawable.ic_av_play,
                    isPlaying ? R.string.pause : R.string.play
            );

            if (isPlaying) {
                view.hideLoadingDialog();
                mediaControl.getDuration(durationListener);
            }
        }

        @Override
        public void onError(ServiceCommandError error) {
            if (error.getCode() == 500) {
                view.hideLoadingDialog();
                view.showBeamFailedDialog();
            }
        }
    };

    private final VolumeControl.VolumeListener volumeListener = new VolumeControl.VolumeListener() {
        @Override
        public void onSuccess(Float volume) {
            view.setVolume((int) (volume * 100.0f));
        }

        @Override
        public void onError(ServiceCommandError error) {
            Timber.d("Volume error");
        }
    };

    private final MediaControl.DurationListener durationListener = new MediaControl.DurationListener() {
        @Override
        public void onError(ServiceCommandError error) {
            Timber.d("Duration error");
        }

        @Override
        public void onSuccess(Long duration) {
            if (totalTimeDuration != duration) {
                totalTimeDuration = duration;
                view.setDuration(duration.intValue());
            }
        }
    };

    private final Runnable positionRunnable = new Runnable() {
        @Override
        public void run() {
            mediaControl.getPosition(new MediaControl.PositionListener() {
                @Override
                public void onSuccess(Long position) {
                    view.displayProgress(position.intValue());

                    if (position > 0) {
                        view.hideLoadingDialog();
                    }
                }

                @Override
                public void onError(ServiceCommandError error) {
                    Timber.d("Position error");
                }
            });
        }
    };

    private final BeamDeviceListener deviceListener = new BeamDeviceListener() {

        @Override
        public void onDeviceDisconnected(ConnectableDevice device) {
            if (mediaControl == null) {
                mediaControl.getPosition(new PositionListener() {
                    @Override public void onSuccess(final Long position) {
                        parentPresenter.fallbackToVideoPlayer(streamInfo, position.intValue());
                    }

                    @Override public void onError(final ServiceCommandError error) {
                        Timber.d("Connected device error");
                    }
                });
            }
        }

    };

}

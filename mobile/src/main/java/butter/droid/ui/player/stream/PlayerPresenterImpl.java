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

package butter.droid.ui.player.stream;

import android.content.Context;
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamDeviceListener;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.ui.player.stream.StreamPlayerPresenterImpl;
import butter.droid.manager.internal.audio.AudioManager;
import butter.droid.manager.internal.brightness.BrightnessManager;
import butter.droid.ui.player.VideoPlayerTouchHandler;
import butter.droid.ui.player.VideoPlayerTouchHandler.OnVideoTouchListener;
import com.connectsdk.device.ConnectableDevice;
import java.util.Locale;

public class PlayerPresenterImpl extends StreamPlayerPresenterImpl implements PlayerPresenter, OnVideoTouchListener {

    private final PlayerView view;
    private final Context context;
    private final PreferencesHandler preferencesHandler;
    private final VideoPlayerTouchHandler touchHandler;
    private final BrightnessManager brightnessManager;
    private final VlcPlayer player;
    private final AudioManager audioManager;
    private final BeamManager beamManager;

    public PlayerPresenterImpl(final PlayerView view, final Context context, final PreferencesHandler preferencesHandler,
            final ProviderManager providerManager, final PlayerManager playerManager, final BeamManager beamManager,
            final BrightnessManager brightnessManager, final AudioManager audioManager, final VideoPlayerTouchHandler touchHandler,
            final VlcPlayer player) {
        super(view, context, preferencesHandler, providerManager, playerManager, player);

        this.view = view;
        this.context = context;
        this.preferencesHandler = preferencesHandler;
        this.touchHandler = touchHandler;
        this.brightnessManager = brightnessManager;
        this.player = player;
        this.audioManager = audioManager;
        this.beamManager = beamManager;
    }

    @Override public void onViewCreated() {
        super.onViewCreated();

        view.setupSubtitles(preferencesHandler.getSubtitleColor(), preferencesHandler.getSubtitleSize(),
                preferencesHandler.getSubtitleStrokeColor(), preferencesHandler.getSubtitleStrokeWidth());

        touchHandler.setListener(this);
    }

    @Override public void onResume() {
        super.onResume();

        displayTitle();
        beamManager.addDeviceListener(deviceListener);
    }

    @Override public void onPause() {
        super.onPause();

        beamManager.removeDeviceListener(deviceListener);
    }

    @Override public void onStop() {
        brightnessManager.restoreBrightness();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        touchHandler.setListener(null);
    }

    @Override public void onProgressChanged(final int progress) {
        if (progress <= (player.getLength() / 100 * getStreamerProgress())) {
            setLastSubtitleCaption(null);
            setCurrentTime(progress);
            progressSubtitleCaption();
        }
    }

    @Override public void onSeekChange(int jump) {
        // Adjust the jump
        long currentTime = getCurrentTime();
        int streamerProgress = getStreamerProgress();
        if ((jump > 0) && ((currentTime + jump) > streamerProgress)) {
            jump = (int) (streamerProgress - currentTime);
        }
        if ((jump < 0) && ((currentTime + jump) < 0)) {
            jump = (int) -currentTime;
        }

        if (streamerProgress > 0) {
            seek(jump);
        }
    }

    @Override public boolean onBrightnessChange(final float delta) {
        brightnessManager.increaseBrightness(delta);
        return true;
    }

    @Override public boolean onVolumeChange(final float delta) {
        int volume = audioManager.setVolumeDelta(delta);

        if (volume != -1) {
            view.showVolumeMessage(volume);
            return true;
        } else {
            return false;
        }
    }

    @Override public void onToggleOverlay() {
        view.toggleOverlay();
    }

    @Override public void hideOverlay() {
        view.hideOverlay();
    }

    private void displayTitle() {
        String title;
        if (media != null) {
            if (this.streamInfo.getQuality() != null) {
                title = String.format(Locale.getDefault(), "%s: %s (%s)", context.getString(R.string.now_playing), media.getTitle(),
                        streamInfo.getQuality());
            } else {
                title = String.format("%s: %s", context.getString(R.string.now_playing), media.getTitle());
            }
        } else {
            title = context.getString(R.string.now_playing);
        }
        view.displayTitle(title);
    }

    private final BeamDeviceListener deviceListener = new BeamDeviceListener() {

        @Override
        public void onDeviceReady(ConnectableDevice device) {
            view.startBeamPlayerActivity(streamInfo, getCurrentTime());

            view.close();
        }
    };

}

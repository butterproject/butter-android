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

import com.connectsdk.device.ConnectableDevice;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamDeviceListener;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.subtitle.SubtitleManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.base.ui.FragmentScope;
import butter.droid.base.ui.player.stream.StreamPlayerPresenterImpl;
import butter.droid.manager.internal.audio.AudioManager;
import butter.droid.manager.internal.brightness.BrightnessManager;
import butter.droid.provider.subs.model.Subtitle;
import butter.droid.ui.media.detail.dialog.subs.SubsPickerParent;
import butter.droid.ui.player.VideoPlayerTouchHandler;
import butter.droid.ui.player.VideoPlayerTouchHandler.OnVideoTouchListener;

@FragmentScope
public class PlayerPresenterImpl extends StreamPlayerPresenterImpl implements PlayerPresenter, OnVideoTouchListener,
        SubsPickerParent {

    private final PlayerView view;
    private final Context context;
    private final VideoPlayerTouchHandler touchHandler;
    private final BrightnessManager brightnessManager;
    private final VlcPlayer player;
    private final AudioManager audioManager;
    @Nullable private final BeamManager beamManager;

    @Inject
    public PlayerPresenterImpl(final PlayerView view, final Context context,
            final PreferencesHandler preferencesHandler,
            final ProviderManager providerManager, @Nullable final BeamManager beamManager,
            final BrightnessManager brightnessManager, final AudioManager audioManager,
            final VideoPlayerTouchHandler touchHandler,
            final VlcPlayer player, final SubtitleManager subtitleManager) {
        super(view, preferencesHandler, providerManager, player, subtitleManager);

        this.view = view;
        this.context = context;
        this.touchHandler = touchHandler;
        this.brightnessManager = brightnessManager;
        this.player = player;
        this.audioManager = audioManager;
        this.beamManager = beamManager;
    }

    @Override public void onViewCreated() {
        super.onViewCreated();

        touchHandler.setListener(this);
    }

    @Override public void onResume() {
        super.onResume();

        displayTitle();

        if (beamManager != null) {
            beamManager.addDeviceListener(deviceListener);
        }
    }

    @Override public void onPause() {
        super.onPause();

        if (beamManager != null) {
            beamManager.removeDeviceListener(deviceListener);
        }
    }

    @Override public void onStop() {
        brightnessManager.restoreBrightness();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        touchHandler.setListener(null);
    }

    @Override public void surfaceChanged(final int width, final int height) {
        player.surfaceChanged(width, height);
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

    @Override public void subtitleSelected(Subtitle subtitle) {
        // TODO saved instant state
        streamInfo.setSubtitle(new SubtitleWrapper(subtitle));
        loadSubtitle();
    }

    private void displayTitle() {
        String title;
        if (streamInfo != null) {
            // TODO: 7/30/17 Do we need this?
//            if (this.streamInfo.getQuality() != null) {
//                title = String.format(Locale.getDefault(), "%s: %s (%s)", context.getString(R.string.now_playing), media.getTitle(),
//                        streamInfo.getQuality());
//            } else {
            title = String.format("%s: %s", context.getString(R.string.now_playing), streamInfo.getFullTitle());
//            }
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

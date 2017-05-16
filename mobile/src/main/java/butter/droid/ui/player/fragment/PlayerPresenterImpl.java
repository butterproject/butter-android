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

package butter.droid.ui.player.fragment;

import android.content.Context;
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenterImpl;
import butter.droid.manager.internal.audio.AudioManager;
import butter.droid.manager.internal.brightness.BrightnessManager;
import butter.droid.ui.player.fragment.VideoPlayerTouchHandler.OnVideoTouchListener;
import java.util.Locale;

public class PlayerPresenterImpl extends BaseVideoPlayerPresenterImpl implements PlayerPresenter, OnVideoTouchListener {

    private final PlayerView view;
    private final Context context;
    private final PreferencesHandler preferencesHandler;
    private final BrightnessManager brightnessManager;
    private final AudioManager audioManager;
    private final VideoPlayerTouchHandler touchHandler;
    private final VlcPlayer player;

    public PlayerPresenterImpl(final PlayerView view, final Context context, final PrefManager prefManager,
            final PreferencesHandler preferencesHandler, final ProviderManager providerManager, final PlayerManager playerManager,
            final BeamManager beamManager, final BrightnessManager brightnessManager, final AudioManager audioManager,
            final VideoPlayerTouchHandler touchHandler, final VlcPlayer player) {
        super(view, context, prefManager, preferencesHandler, providerManager, playerManager, beamManager, player);
        this.view = view;
        this.context = context;
        this.brightnessManager = brightnessManager;
        this.audioManager = audioManager;
        this.preferencesHandler = preferencesHandler;
        this.touchHandler = touchHandler;
        this.player = player;
    }

    @Override public void onResume() {
        super.onResume();

        displayTitle();
    }

    @Override protected void onHardwareAccelerationError() {
        view.onHardwareAccelerationError();
        disableHardwareAcceleration();
    }

    @Override protected void updateSubtitleSize(final int size) {
        view.updateSubtitleSize(size);
    }

    @Override protected void startBeamPlayerActivity() {
        view.startBeamPlayerActivity(streamInfo, getCurrentTime());
    }

    @Override public void onViewCreated() {
        super.onViewCreated();

        view.setupSubtitles(preferencesHandler.getSubtitleColor(), preferencesHandler.getSubtitleSize(),
                preferencesHandler.getSubtitleStrokeColor(), preferencesHandler.getSubtitleStrokeWidth());

        touchHandler.setListener(this);
    }

    @Override public void onProgressChanged(final int progress) {
        if (progress <= player.getLength()) {
//        if (progress <= (player.getLength() / 100 * getStreamerProgress())) {
            setLastSubtitleCaption(null);
            setCurrentTime(progress);
            progressSubtitleCaption();
        }
    }

    @Override public void onStop() {
        brightnessManager.restoreBrightness();
    }

    @Override public void requestDisableHardwareAcceleration() {
        disableHardwareAcceleration();
        loadMedia();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        touchHandler.setListener(null);
    }

    @Override public void streamProgressUpdated(final float progress) {
        super.streamProgressUpdated(progress);
        view.displayStreamProgress(getStreamerProgress());
    }

    private void displayTitle() {
        String title;
        if (media != null && media.title != null) {
            if (null != this.streamInfo.getQuality()) {
                title = String.format(Locale.getDefault(), "%s: %s (%s)", context.getString(R.string.now_playing), media.title,
                        streamInfo.getQuality());
            } else {
                title = String.format("%s: %s", context.getString(R.string.now_playing), media.title);
            }
        } else {
            title = context.getString(R.string.now_playing);
        }
        view.displayTitle(title);
    }

    @Override public void onSeekChange(final int jump) {
//        // Adjust the jump
//        if ((jump > 0) && ((getCurrentTime() + jump) > controlBar.getSecondaryProgress())) {
//            jump = (int) (controlBar.getSecondaryProgress() - getCurrentTime());
//        }
//        if ((jump < 0) && ((getCurrentTime() + jump) < 0)) {
//            jump = (int) -getCurrentTime();
//        }
//
//        long currentTime = getCurrentTime();
//        if (seek && controlBar.getSecondaryProgress() > 0) {
//            seek(jump);
//        }
//
//        if (getDuration() > 0) {
//            showPlayerInfo(String.format("%s%s (%s)", jump >= 0 ? "+" : "", StringUtils.millisToString(jump), StringUtils.millisToString(currentTime + jump)));
//        }
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
}

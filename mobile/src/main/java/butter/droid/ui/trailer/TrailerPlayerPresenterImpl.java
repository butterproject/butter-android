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

package butter.droid.ui.trailer;

import android.content.Context;
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.phone.PhoneManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.internal.youtube.YouTubeManager;
import butter.droid.base.manager.network.NetworkManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.ui.trailer.BaseTrailerPlayerPresenterImpl;
import butter.droid.manager.internal.audio.AudioManager;
import butter.droid.manager.internal.brightness.BrightnessManager;
import butter.droid.ui.player.VideoPlayerTouchHandler;
import butter.droid.ui.player.VideoPlayerTouchHandler.OnVideoTouchListener;

public class TrailerPlayerPresenterImpl extends BaseTrailerPlayerPresenterImpl implements TrailerPlayerPresenter, OnVideoTouchListener {

    private final TrailerPlayerView view;
    private final Context context;
    private final VlcPlayer player;
    private final BrightnessManager brightnessManager;
    private final VideoPlayerTouchHandler touchHandler;
    private final AudioManager audioManager;

    public TrailerPlayerPresenterImpl(final TrailerPlayerView view, final Context context, final PrefManager prefManager,
            final PreferencesHandler preferencesHandler, final VlcPlayer player, final YouTubeManager youTubeManager,
            final NetworkManager networkManager, final PhoneManager phoneManager, final BrightnessManager brightnessManager,
            final VideoPlayerTouchHandler touchHandler, final AudioManager audioManager) {
        super(view, prefManager, preferencesHandler, player, youTubeManager, networkManager, phoneManager);

        this.view = view;
        this.context = context;
        this.player = player;
        this.brightnessManager = brightnessManager;
        this.touchHandler = touchHandler;
        this.audioManager = audioManager;
    }

    @Override public void onViewCreated() {
        super.onViewCreated();
        touchHandler.setListener(this);
    }

    @Override public void onResume() {
        super.onResume();

        displayTitle();
    }

    @Override public void onProgressChanged(final int progress) {
        if (progress <= player.getLength()) {
//        if (progress <= (player.getLength() / 100 * getStreamerProgress())) {
            setCurrentTime(progress);
        }
    }

    @Override public void onStop() {
        brightnessManager.restoreBrightness();
    }

    @Override public void onDestroy() {
        super.onDestroy();

        touchHandler.setListener(null);
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

    private void displayTitle() {
        String title;
        if (media != null && media.title != null) {
            title = String.format("%s: %s", context.getString(R.string.now_playing), media.title);
        } else {
            title = context.getString(R.string.now_playing);
        }
        view.displayTitle(title);
    }
}

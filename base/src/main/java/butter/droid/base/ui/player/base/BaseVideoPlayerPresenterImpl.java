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

package butter.droid.base.ui.player.base;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.internal.vlc.VlcPlayer.PlayerCallback;
import timber.log.Timber;

public abstract class BaseVideoPlayerPresenterImpl implements BaseVideoPlayerPresenter, PlayerCallback {

    private final BaseVideoPlayerView view;
    private final PreferencesHandler preferencesHandler;
    private final VlcPlayer player;

    private long resumePosition;

    public BaseVideoPlayerPresenterImpl(final BaseVideoPlayerView view, final PreferencesHandler preferencesHandler,
            final VlcPlayer player) {
        this.view = view;
        this.preferencesHandler = preferencesHandler;
        this.player = player;
    }

    protected void onCreate(final long resumePosition) {
        if (!player.initialize()) {
            view.close();
            Timber.e("Error initializing media player");
            return;
        }

        player.setCallback(this);

        this.resumePosition = resumePosition;

    }

    @Override public void onResume() {
        prepareVlcVout();
    }

    @Override public void onPause() {
        resumePosition = player.getTime();
        player.stop();

        player.detachFromSurface();
        view.detachMediaSession();
    }

    @Override public void onSaveInstanceState(final Bundle outState) {
        view.saveState(outState, resumePosition);
    }

    @Override public void onDestroy() {
        player.release();
        player.setCallback(null);
    }

    @Override public void play() {
        player.play();
    }

    @Override public void pause() {
        player.pause();
    }

    @Override public void onScaleClicked() {
        int currentPolicy = player.getSizePolicy();
        if (currentPolicy < SURFACE_ORIGINAL) {
            currentPolicy++;
        } else {
            currentPolicy = SURFACE_BEST_FIT;
        }

        player.setSizePolicy(currentPolicy);
        view.showOverlay();
    }

    @Override public void seekForwardClick() {
        seek(10000);
    }

    @Override public void seekBackwardClick() {
        seek(-10000);
    }

    /**
     * External extras: - position (long) - position of the video to start with (in ms)
     */
    protected void loadMedia(@NonNull Uri uri) {

        int ha = preferencesHandler.getHwAcceleration();

        player.loadMedia(uri, ha);

        if (resumePosition > 0) {
            player.setTime(resumePosition);
        }

        player.play();
    }

    protected void loadSubs(@NonNull Uri uri) {
        player.loadSubs(uri);
    }

    protected long getCurrentTime() {
        return player.getTime();
    }

    protected void seek(int delta) {
        if (player.getLength() <= 0) {
            return;
        }

        long position = player.getTime() + delta;
        if (position < 0) {
            position = 0;
        }

        setCurrentTime(position);
        view.showOverlay();
    }

    protected void setCurrentTime(long time) {
        if (time <= player.getLength()) {
            player.setTime(time);
        }
    }

    /**
     * Is a video currently playing with VLC
     *
     * @return true if video is played using VLC
     */
    protected boolean isPlaying() {
        return player.isPlaying();
    }

    @Override public void updateSurfaceSize(final int width, final int height) {
        view.updateSurfaceSize(width, height);
    }

    @Override public void progressChanged(final long progress) {
        updateControls();
    }

    @Override public void playing() {
        updateControls();
        view.setKeepScreenOn(true);
        view.setProgressVisible(false);
        view.showOverlay();
    }

    @Override public void paused() {
        updateControls();
        view.setKeepScreenOn(false);
    }

    @Override public void stopped() {
        view.setKeepScreenOn(false);
        updateControls();
    }

    @Override public void endReached() {
        view.onPlaybackEndReached();
    }

    @Override public void playerError() {
        view.onErrorEncountered();
    }

    private void prepareVlcVout() {
        view.attachVlcViews();
    }

    private void updateControls() {
        view.updateControlsState(player.isPlaying(), getCurrentTime(), player.getLength());
    }

}

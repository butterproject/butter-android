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

import static butter.droid.base.manager.internal.vlc.VLCOptions.HW_ACCELERATION_DISABLED;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.manager.internal.vlc.PlayerManager;
import butter.droid.base.manager.internal.vlc.VlcPlayer;
import butter.droid.base.manager.internal.vlc.VlcPlayer.PlayerCallback;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.subs.SubtitleDownloader.ISubtitleDownloaderListener;

public abstract class BaseVideoPlayerPresenterImpl implements BaseVideoPlayerPresenter, ISubtitleDownloaderListener, PlayerCallback {

    private static final String PREF_RESUME_POSITION = "resume_position";

    private final BaseVideoPlayerView view;
    private final Context context;
    private final PrefManager prefManager;
    private final PreferencesHandler preferencesHandler;
    private final ProviderManager providerManager;
    private final PlayerManager playerManager;
    private final BeamManager beamManager;
    private final VlcPlayer player;

    private long resumePosition;
    protected Media media;

    // probably required when hardware acceleration selection during playback is implemented
    private boolean disabledHardwareAcceleration;

    private int streamerProgress;

    public BaseVideoPlayerPresenterImpl(final BaseVideoPlayerView view, final Context context, final PrefManager prefManager,
            final PreferencesHandler preferencesHandler, final ProviderManager providerManager, final PlayerManager playerManager,
            final BeamManager beamManager, final VlcPlayer player) {
        this.view = view;
        this.context = context;
        this.prefManager = prefManager;
        this.preferencesHandler = preferencesHandler;
        this.providerManager = providerManager;
        this.playerManager = playerManager;
        this.beamManager = beamManager;
        this.player = player;
    }

    protected void onCreate(final Media media, final long resumePosition) {

        if (!player.initialize()) {
            // TODO: 4/2/17 Stop activity & maybe show error
            return;
        }

        player.setCallback(this);

        this.resumePosition = resumePosition;
        this.media = media;

        prefManager.save(PREF_RESUME_POSITION, resumePosition);

    }

    @Override public void onResume() {
        prepareVlcVout();
    }

    @Override public void onPause() {
        saveVideoCurrentTime();
        player.stop();

        player.detachFromSurface();
        view.detachMediaSession();
    }

    @Override public void onDestroy() {
        prefManager.save(PREF_RESUME_POSITION, 0);

        player.release();
        player.setCallback(null);
    }

    @Override public void play() {
        player.play();
    }

    @Override public void pause() {
        player.pause();
    }

    @Override public void streamProgressUpdated(final float progress) {
        int newProgress = (int) ((player.getLength() / 100) * progress);
        if (streamerProgress < newProgress) {
            streamerProgress = newProgress;
        }
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


    protected abstract void onHardwareAccelerationError();

    /**
     * Called when subtitle for current media successfully loaded or disabled.
     *
     * @param enabled Whether subtitle is loaded or disabled.
     */
    protected void onSubtitleEnabledStateChanged(boolean enabled) {
        // override if needed
    }

    /**
     * External extras: - position (long) - position of the video to start with (in ms)
     */
    protected void loadMedia(@NonNull Uri uri) {

        int ha;
        if (disabledHardwareAcceleration) {
            ha = HW_ACCELERATION_DISABLED;
        } else {
            ha = preferencesHandler.getHwAcceleration();
        }

        player.loadMedia(uri, ha);

        long resumeFrom = prefManager.get(PREF_RESUME_POSITION, resumePosition);
        if (resumeFrom > 0) {
            player.setTime(resumeFrom);
        }

        player.play();
    }

    protected void disableHardwareAcceleration() {
        disabledHardwareAcceleration = true;
        saveVideoCurrentTime();
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
//        view.onProgressChanged(getCurrentTime(), getStreamerProgress(), player.getLength()); // TODO: 4/2/17 Is this already handled by vlc event?
    }

    protected void setCurrentTime(long time) {
        if (time / player.getLength() * 100 <= getStreamerProgress()) {
            player.setTime(time);
        }
    }

    protected int getStreamerProgress() {
        return streamerProgress;
    }

    private void saveVideoCurrentTime() {
        long currentTime = player.getTime();
        prefManager.save(PREF_RESUME_POSITION, currentTime);
    }

    /**
     * Is a video currently playing with VLC
     *
     * @return true if video is played using VLC
     */
    protected boolean isPlaying() {
        return player.isPlaying();
    }

    private void handleHardwareAccelerationError() {
        saveVideoCurrentTime();
        player.stop();
        onHardwareAccelerationError();
    }

    private void prepareVlcVout() {
       view.attachVlcViews();
    }

    private void resumeVideo() {
        long resumePosition = prefManager.get(PREF_RESUME_POSITION, 0);
        if (player.getLength() > resumePosition && resumePosition > 0) {
            setCurrentTime(resumePosition);
            prefManager.save(PREF_RESUME_POSITION, 0);
        }
    }

    @Override public void updateSurfaceSize(final int width, final int height) {
        view.updateSurfaceSize(width, height);
    }

    @Override public void progressChanged(final long progress) {
        updateControls();
    }

    @Override public void playing() {
        updateControls();
    }

    @Override public void paused() {
        updateControls();
    }

    @Override public void stopped() {

    }

    private void updateControls() {
        view.updateControlsState(player.isPlaying(), getCurrentTime(), getStreamerProgress(), player.getLength());
    }

//            switch (event.type) {
//        case MediaPlayer.Event.Playing:
//            videoDuration = player.getLength();
//            view.setKeepScreenOn(true);
//            resumeVideo();
//            view.setProgressVisible(false);
//            view.showOverlay();
//            view.updatePlayPauseState(true);
//            break;
//        case MediaPlayer.Event.Paused:
//            view.setKeepScreenOn(false);
//            saveVideoCurrentTime();
//            view.updatePlayPauseState(false);
//            break;
//        case MediaPlayer.Event.Stopped:
//            view.setKeepScreenOn(false);
//            view.updatePlayPauseState(false);
//            break;
//        case MediaPlayer.Event.EndReached:
//            endReached();
//            view.updatePlayPauseState(false);
//            break;
//        case MediaPlayer.Event.EncounteredError:
//            view.onErrorEncountered();
//            view.updatePlayPauseState(false);
//            break;
//        case MediaPlayer.Event.Opening:
//            view.setProgressVisible(true);
//            videoDuration = player.getLength();
////                mediaPlayer.play(); // should be handled by auto plau
//            break;
//        case MediaPlayer.Event.TimeChanged:
//        case MediaPlayer.Event.PositionChanged:
//            view.onProgressChanged(getCurrentTime(), getStreamerProgress(), getDuration());
//            progressSubtitleCaption();
//            break;

    @Override public void endReached() {
        view.onPlaybackEndReached();
    }

    @Override public void playerError() {
        handleHardwareAccelerationError();
    }

}

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
import android.media.AudioManager;
import butter.droid.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.beaming.BeamManager;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.manager.vlc.PlayerManager;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.base.ui.player.fragment.BaseVideoPlayerPresenterImpl;
import java.util.Locale;
import org.videolan.libvlc.LibVLC;

public class VideoPlayerFPresenterImpl extends BaseVideoPlayerPresenterImpl implements VideoPlayerFPresenter {

    private final VideoPlayerFView view;
    private final Context context;
    private final AudioManager audioManager;

    private final int audioMax;

    public VideoPlayerFPresenterImpl(final VideoPlayerFView view,
            final Context context, final PrefManager prefManager,
            final LibVLC libVLC, final PreferencesHandler preferencesHandler,
            final ProviderManager providerManager,
            final PlayerManager playerManager, final BeamManager beamManager, final AudioManager audioManager) {
        super(view, context, prefManager, libVLC, preferencesHandler, providerManager, playerManager, beamManager);
        this.view = view;
        this.context = context;
        this.audioManager = audioManager;

        audioMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override public void onCreate(final StreamInfo streamInfo, final long resumePosition) {
        super.onCreate(streamInfo, resumePosition);

        displayTitle();
    }

    @Override protected void onHardwareAccelerationError() {

    }

    @Override protected void updateSubtitleSize(final int size) {

    }

    @Override protected void startBeamPlayerActivity() {

    }

    @Override public void onViewCreated() {

    }

    @Override public void onProgressChanged(final int progress) {
        if (isSeeking() && progress <= (getDuration() / 100 * getStreamerProgress())) {
            setLastSubtitleCaption(null);
            setCurrentTime(progress);
            view.onProgressChanged(getCurrentTime(), getStreamerProgress(),
                    getDuration()); // TODO: 4/2/17 SIs this already handled by VLC event?
            progressSubtitleCaption();
        }
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
}

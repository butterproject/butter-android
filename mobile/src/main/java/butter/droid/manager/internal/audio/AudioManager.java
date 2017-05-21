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

package butter.droid.manager.internal.audio;

import dagger.Reusable;
import javax.inject.Inject;

@Reusable
public class AudioManager {

    private final static int AUDIO_STREAM = android.media.AudioManager.STREAM_MUSIC;

    private final android.media.AudioManager audioManager;

    private final int maxVolume;

    @Inject public AudioManager(final android.media.AudioManager audioManager) {
        this.audioManager = audioManager;

        maxVolume = getMaxVolume();
    }

    public int setVolumeDelta(float deltaFraction) {
        int increase = (int) (deltaFraction * maxVolume);

        if (increase != 0) {
            int volume = getAudioVolume() + increase;
            volume = Math.min(Math.max(volume, 0), maxVolume);

            setAudioVolume(volume);

            return volume;
        } else {
            return -1;
        }

    }

    private void setAudioVolume(int vol) {
        audioManager.setStreamVolume(AUDIO_STREAM, vol, 0);

        /* Since android 4.3, the safe volume warning dialog is displayed only with the FLAG_SHOW_UI flag.
         * We don't want to always show the default UI volume, so show it only when volume is not set. */
        int newVol = audioManager.getStreamVolume(AUDIO_STREAM);
        if (vol != newVol) {
            audioManager.setStreamVolume(AUDIO_STREAM, vol, android.media.AudioManager.FLAG_SHOW_UI);
        }
    }

    private int getAudioVolume() {
        return audioManager.getStreamVolume(AUDIO_STREAM);
    }

    private int getMaxVolume() {
        return audioManager.getStreamMaxVolume(AUDIO_STREAM);
    }

}

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

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public interface BeamPlayerView {

    void tintProgress(@ColorInt int paletteColor);

    void loadCoverImage(@NonNull String imageUrl);

    void hideSeekBar();

    void disableVolumePanel();

    void disablePlayButton();

    void showErrorMessage(@StringRes int errorMessage);

    void closeScreen();

    void setVolume(int volume);

    void displayProgress(int progress);

    void setDuration(int duration);

    void hideLoadingDialog();

    void showBeamFailedDialog();

    void updatePlayButton(@DrawableRes int icon, @StringRes int cd);

    void startNotificationService(boolean isPlaying);
}

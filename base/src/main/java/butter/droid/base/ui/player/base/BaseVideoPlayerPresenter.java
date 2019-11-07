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

import android.os.Bundle;
import androidx.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface BaseVideoPlayerPresenter {

    void onResume();

    void onPause();

    void onViewCreated();

    void onDestroy();

    void play();

    void pause();

    void onScaleClicked();

    void onSaveInstanceState(Bundle outState);

    void seekTo(long pos);

    // region IntDef

    @IntDef({SURFACE_BEST_FIT, SURFACE_FIT_SCREEN, SURFACE_FILL, SURFACE_16_9, SURFACE_4_3, SURFACE_ORIGINAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SizePolicy {

    }

    int SURFACE_BEST_FIT = 0;
    int SURFACE_FIT_SCREEN = 1;
    int SURFACE_FILL = 2;
    int SURFACE_16_9 = 3;
    int SURFACE_4_3 = 4;
    int SURFACE_ORIGINAL = 5;

    // endregion IntDef

}

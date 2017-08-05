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

package butter.droid.ui.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import butter.droid.ui.main.MainPresenterImpl.OnGenreChangeListener;
import butter.droid.ui.main.genre.list.model.UiGenre;

public interface MainPresenter {

    void onCreate(final int selectedProviderId);

    void onResume();

    void storagePermissionDenied();

    void storagePermissionGranted();

    void selectProvider(int providerId);

    void openMenuActivity(Class<? extends Activity> activityClass);

    void onGenreChanged(UiGenre genre);

    void addGenreListener(OnGenreChangeListener listener);

    void removeGenreListener(OnGenreChangeListener listener);

    void onSaveInstanceState(@NonNull Bundle outState);

    void searchClicked();

    void onDestroy();
}

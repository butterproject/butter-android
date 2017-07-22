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

package butter.droid.ui.main.genre.list.model;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import butter.droid.provider.base.filter.Genre;

public class UiGenre {

    @NonNull private final Genre genre;
    private boolean selected;

    public UiGenre(@NonNull Genre genre) {
        this.genre = genre;
    }

    @StringRes public int getLabel() {
        return genre.getName();
    }

    public boolean isSelected() {
        return selected;
    }

    @NonNull public Genre getGenre() {
        return genre;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

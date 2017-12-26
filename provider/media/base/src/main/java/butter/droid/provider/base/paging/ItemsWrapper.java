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

package butter.droid.provider.base.paging;

import android.support.annotation.NonNull;
import butter.droid.provider.base.model.Media;
import java.util.List;

public class ItemsWrapper {

    @NonNull private final List<Media> media;
    @NonNull private final Paging paging;

    public ItemsWrapper(@NonNull final List<Media> media, @NonNull final Paging paging) {
        this.media = media;
        this.paging = paging;
    }

    @NonNull public List<Media> getMedia() {
        return media;
    }

    @NonNull public Paging getPaging() {
        return paging;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ItemsWrapper that = (ItemsWrapper) o;

        if (!media.equals(that.media)) {
            return false;
        }
        return paging.equals(that.paging);
    }

    @Override public int hashCode() {
        int result = media.hashCode();
        result = 31 * result + paging.hashCode();
        return result;
    }
}

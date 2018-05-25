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

package butter.droid.provider.filter;

import androidx.annotation.Nullable;

public class Pager {

    @Nullable private final String endCursor;

    public Pager(@Nullable final String endCursor) {
        this.endCursor = endCursor;
    }

    @Nullable public String getEndCursor() {
        return endCursor;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pager)) {
            return false;
        }

        final Pager pager = (Pager) o;

        return endCursor != null ? endCursor.equals(pager.endCursor) : pager.endCursor == null;
    }

    @Override public int hashCode() {
        return endCursor != null ? endCursor.hashCode() : 0;
    }

}

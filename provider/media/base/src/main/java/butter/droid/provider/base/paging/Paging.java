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

import android.support.annotation.Nullable;

public class Paging {

    @Nullable private final String endCursor;
    private final boolean hasNextPage;

    public Paging(@Nullable final String endCursor, final boolean hasNextPage) {
        this.endCursor = endCursor;
        this.hasNextPage = hasNextPage;
    }

    @Nullable public String getEndCursor() {
        return endCursor;
    }

    public boolean hasNextPage() {
        return hasNextPage;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Paging paging = (Paging) o;

        if (hasNextPage != paging.hasNextPage) {
            return false;
        }
        return endCursor != null ? endCursor.equals(paging.endCursor) : paging.endCursor == null;
    }

    @Override public int hashCode() {
        int result = endCursor != null ? endCursor.hashCode() : 0;
        result = 31 * result + (hasNextPage ? 1 : 0);
        return result;
    }
}

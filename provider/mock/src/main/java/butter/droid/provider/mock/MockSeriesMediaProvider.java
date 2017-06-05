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

package butter.droid.provider.mock;

import android.support.annotation.NonNull;
import butter.droid.provider.AbsMediaProvider;
import butter.droid.provider.base.ItemsWrapper;
import butter.droid.provider.base.Media;
import io.reactivex.Single;

public class MockSeriesMediaProvider extends AbsMediaProvider {

    @NonNull @Override public Single<ItemsWrapper> items() {
        return null;
    }

    @NonNull @Override public Single<Media> detail(Media media) {
        return null;
    }
}

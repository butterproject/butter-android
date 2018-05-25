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

package butter.droid.provider.subs;

import android.net.Uri;
import androidx.annotation.NonNull;

import java.util.List;

import butter.droid.provider.base.model.Media;
import butter.droid.provider.subs.model.Subtitle;
import io.reactivex.Maybe;
import io.reactivex.Single;

public interface SubsProvider {

    Single<List<Subtitle>> list(@NonNull Media media);

    Maybe<Subtitle> getSubtitle(@NonNull Media media, @NonNull String languageCode);

    Maybe<Uri> downloadSubs(@NonNull Media media, @NonNull Subtitle subtitle);

}

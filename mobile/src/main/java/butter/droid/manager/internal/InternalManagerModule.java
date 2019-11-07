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

package butter.droid.manager.internal;

import android.content.Context;

import androidx.annotation.Nullable;
import butter.droid.base.BuildConfig;
import butter.droid.base.Internal;
import butter.droid.base.manager.internal.beaming.BeamManager;
import dagger.Module;
import dagger.Provides;

@Module
public class InternalManagerModule {

    @Provides @Internal @Nullable BeamManager provideBeamManager(final Context context) {
        if (BuildConfig.FEATURE_MOBILE_BEAM) {
            return new BeamManager(context);
        } else {
            return null;
        }
    }

}

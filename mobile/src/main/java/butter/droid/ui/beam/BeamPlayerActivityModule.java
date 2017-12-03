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

package butter.droid.ui.beam;

import butter.droid.base.manager.internal.beaming.BeamManager;
import butter.droid.base.ui.ActivityScope;
import butter.droid.base.ui.FragmentScope;
import butter.droid.ui.beam.BeamPlayerActivityModule.BeamPlayerActivityBindModule;
import butter.droid.ui.beam.fragment.BeamPlayerFragment;
import butter.droid.ui.beam.fragment.BeamPlayerModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module(includes = BeamPlayerActivityBindModule.class)
public class BeamPlayerActivityModule {

    @Provides @ActivityScope BeamPlayerActivityPresenter providePresenter(BeamPlayerActivityView view, BeamManager beamManager) {
        return new BeamPlayerActivityPresenterImpl(view, beamManager);
    }

    @Module
    public interface BeamPlayerActivityBindModule {
        @Binds BeamPlayerActivityView bindView(BeamPlayerActivity activity);

        @FragmentScope
        @ContributesAndroidInjector(modules = BeamPlayerModule.class)
        BeamPlayerFragment contributeBeamPlayerFragmentInjector();
    }
}

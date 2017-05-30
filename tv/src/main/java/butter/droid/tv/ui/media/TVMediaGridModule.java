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

package butter.droid.tv.ui.media;

import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.base.ui.FragmentScope;
import butter.droid.tv.manager.internal.background.BackgroundUpdaterModule;
import dagger.Module;
import dagger.Provides;

@Module(includes = BackgroundUpdaterModule.class)
public class TVMediaGridModule {

    private final TVMediaGridView view;

    public TVMediaGridModule(final TVMediaGridView view) {
        this.view = view;
    }

    @Provides @FragmentScope TVMediaGridView provideView() {
        return view;
    }

    @Provides @FragmentScope TVMediaGridPresenter providePresenter(TVMediaGridView view, ProviderManager providerManager) {
        return new TVMediaGridPresenterImpl(view, providerManager);
    }
}

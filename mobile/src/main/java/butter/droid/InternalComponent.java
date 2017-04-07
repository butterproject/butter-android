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

package butter.droid;

import butter.droid.activities.SearchActivity;
import butter.droid.base.BaseInternalComponent;
import butter.droid.base.Internal;
import butter.droid.base.providers.ProviderComponent;
import butter.droid.fragments.dialog.BeamDeviceSelectorDialogFragment;
import butter.droid.fragments.dialog.EpisodeDialogFragment;
import butter.droid.fragments.dialog.LoadingDetailDialogFragment;
import butter.droid.ui.about.AboutActivity;
import butter.droid.ui.about.AboutComponent;
import butter.droid.ui.beam.BeamPlayerActivityComponent;
import butter.droid.ui.loading.StreamLoadingComponent;
import butter.droid.ui.main.MainComponent;
import butter.droid.ui.media.detail.MediaDetailComponent;
import butter.droid.ui.player.VideoPlayerComponent;
import butter.droid.ui.preferences.PreferencesComponent;
import butter.droid.ui.search.SearchComponent;
import butter.droid.ui.terms.TermsComponent;
import butter.droid.ui.trailer.TrailerPlayerComponent;
import dagger.Component;

@Internal @Component(
        dependencies = {
                ProviderComponent.class
        },
        modules = InternalModule.class
)
public interface InternalComponent extends BaseInternalComponent {

    void inject(MobileButterApplication application);

    void inject(AboutActivity activity);

    void inject(SearchActivity activity);

    void inject(LoadingDetailDialogFragment fragment);

    void inject(EpisodeDialogFragment fragment);

    void inject(BeamDeviceSelectorDialogFragment fragment);

    MainComponent.Builder mainComponentBuilder();

    PreferencesComponent.Builder preferencesComponentBuilder();

    TermsComponent.Builder termsComponentBuilder();

    MediaDetailComponent.Builder mediaDetailsComponentBuilder();

    AboutComponent.Builder aboutComponentBuilder();

    StreamLoadingComponent.Builder streamLoadingComponentBuilder();

    SearchComponent.Builder searchComponentBuilder();

    TrailerPlayerComponent.Builder trailerComponentBuilder();

    BeamPlayerActivityComponent.Builder beamPlayerActivityComponentBuilder();

    VideoPlayerComponent.Builder videoPlayerComponentBuilder();

}

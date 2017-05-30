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

package butter.droid.tv;

import butter.droid.base.BaseInternalComponent;
import butter.droid.base.Internal;
import butter.droid.base.providers.ProviderComponent;
import butter.droid.tv.activities.TVMediaDetailActivity;
import butter.droid.tv.activities.TVPreferencesActivity;
import butter.droid.tv.activities.TVUpdateActivity;
import butter.droid.tv.fragments.TVMovieDetailsFragment;
import butter.droid.tv.fragments.TVShowDetailsFragment;
import butter.droid.tv.fragments.TVUpdateFragment;
import butter.droid.tv.service.RecommendationService;
import butter.droid.tv.service.recommendation.RecommendationContentProvider;
import butter.droid.tv.ui.about.TVAboutComponent;
import butter.droid.tv.ui.launch.TVLaunchComponent;
import butter.droid.tv.ui.loading.TVStreamLoadingComponent;
import butter.droid.tv.ui.main.TVMainComponent;
import butter.droid.tv.ui.media.TVMediaGridActivity;
import butter.droid.tv.ui.media.TVMediaGridComponent;
import butter.droid.tv.ui.player.TVVideoPlayerComponent;
import butter.droid.tv.ui.preferences.TVPreferencesComponent;
import butter.droid.tv.ui.search.TVSearchActivity;
import butter.droid.tv.ui.search.TVSearchComponent;
import butter.droid.tv.ui.terms.TVTermsActivity;
import butter.droid.tv.ui.terms.TVTermsComponent;
import butter.droid.tv.ui.trailer.TVTrailerPlayerActivity;
import butter.droid.tv.ui.trailer.TVTrailerPlayerComponent;
import dagger.Component;

@Internal @Component(
        dependencies = {
                ProviderComponent.class
        },
        modules = TVInternalModule.class
)
public interface TVInternalComponent extends BaseInternalComponent {

    void inject(TVButterApplication application);

    void inject(TVMediaDetailActivity activity);

    void inject(TVMediaGridActivity activity);

    void inject(TVPreferencesActivity activity);

    void inject(TVSearchActivity activity);

    void inject(TVUpdateActivity activity);

    void inject(TVTermsActivity activity);

    void inject(TVTrailerPlayerActivity activity);

    void inject(RecommendationService service);

    void inject(TVMovieDetailsFragment fragment);

    void inject(TVShowDetailsFragment fragment);

    void inject(TVUpdateFragment fragment);

    void inject(RecommendationContentProvider contentProvider);

    TVPreferencesComponent.Builder preferencesComponentBuilder();

    TVTermsComponent.Builder termsComponentBuilder();

    TVAboutComponent.Builder aboutComponentBuilder();

    TVStreamLoadingComponent.Builder streamLoadingComponentBuilder();

    TVSearchComponent.Builder searchComponentBuilder();

    TVTrailerPlayerComponent.Builder tvTrailerPlayerComponentBuilder();

    TVVideoPlayerComponent.Builder tvVideoPlayerComponentBuilder();

    TVLaunchComponent.Builder tvLaunchComponentBuilder();

    TVMainComponent.Builder tvMainComponentBuilder();

    TVMediaGridComponent.Builder tvMediaGridComponentBuilder();

}

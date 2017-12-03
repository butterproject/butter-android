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

package butter.droid.tv.ui;

import butter.droid.base.torrent.ServiceScope;
import butter.droid.base.ui.ActivityScope;
import butter.droid.base.ui.BaseInjectorModule;
import butter.droid.base.ui.FragmentScope;
import butter.droid.tv.service.RecommendationModule;
import butter.droid.tv.service.RecommendationService;
import butter.droid.tv.service.recommendation.RecommendationContentProvider;
import butter.droid.tv.service.recommendation.RecommendationContentProviderModule;
import butter.droid.tv.ui.about.TVAboutFragment;
import butter.droid.tv.ui.about.TVAboutModule;
import butter.droid.tv.ui.detail.TVMediaDetailActivity;
import butter.droid.tv.ui.detail.TVMediaDetailModule;
import butter.droid.tv.ui.launch.TVLaunchActivity;
import butter.droid.tv.ui.launch.TVLaunchModule;
import butter.droid.tv.ui.loading.TVStreamLoadingActivity;
import butter.droid.tv.ui.loading.TVStreamLoadingModule;
import butter.droid.tv.ui.main.TVMainActivity;
import butter.droid.tv.ui.main.TVMainModule;
import butter.droid.tv.ui.media.TVMediaGridActivity;
import butter.droid.tv.ui.media.TVMediaGridActiviyModule;
import butter.droid.tv.ui.player.TVVideoPlayerActivity;
import butter.droid.tv.ui.player.TVVideoPlayerModule;
import butter.droid.tv.ui.preferences.TVPreferencesActivity;
import butter.droid.tv.ui.preferences.TVPreferencesActivityModule;
import butter.droid.tv.ui.preferences.TVPreferencesFragment;
import butter.droid.tv.ui.preferences.TVPreferencesModule;
import butter.droid.tv.ui.search.TVSearchActivity;
import butter.droid.tv.ui.search.TVSearchActivityModule;
import butter.droid.tv.ui.terms.TVTermsActivity;
import butter.droid.tv.ui.terms.TVTermsActivityModule;
import butter.droid.tv.ui.terms.TVTermsFragment;
import butter.droid.tv.ui.terms.TVTermsModule;
import butter.droid.tv.ui.trailer.TVTrailerPlayerActivity;
import butter.droid.tv.ui.trailer.TVTrailerPlayerActivityModule;
import butter.droid.tv.ui.update.TVUpdateActivity;
import butter.droid.tv.ui.update.TVUpdateActivityModule;
import butter.droid.tv.ui.update.TVUpdateFragment;
import butter.droid.tv.ui.update.TVUpdateModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(includes = BaseInjectorModule.class)
public abstract class TVInjectorModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = TVPreferencesActivityModule.class)
    abstract TVPreferencesActivity contributeTVPreferenesActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVStreamLoadingModule.class)
    abstract TVStreamLoadingActivity contributeTVStreamLoadingActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVUpdateActivityModule.class)
    abstract TVUpdateActivity contributeTVUpdateActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVLaunchModule.class)
    abstract TVLaunchActivity contributeTVLaunchActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVTermsActivityModule.class)
    abstract TVTermsActivity contributeTVTermsActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVTrailerPlayerActivityModule.class)
    abstract TVTrailerPlayerActivity contributeTVTrailerPlayerActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVSearchActivityModule.class)
    abstract TVSearchActivity contributeTVSearchActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVMediaDetailModule.class)
    abstract TVMediaDetailActivity contributeTVMediaDetailActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVMainModule.class)
    abstract TVMainActivity contributeTVMainActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVVideoPlayerModule.class)
    abstract TVVideoPlayerActivity contributeTVVideoPlayerActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TVMediaGridActiviyModule.class)
    abstract TVMediaGridActivity contributeTVMediaGridActivityInjector();

    @FragmentScope
    @ContributesAndroidInjector(modules = TVPreferencesModule.class)
    abstract TVPreferencesFragment contributeTVPreferenesFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector(modules = TVUpdateModule.class)
    abstract TVUpdateFragment contributeTVUpdateFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector(modules = TVTermsModule.class)
    abstract TVTermsFragment contributeTVTermsFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector(modules = TVAboutModule.class)
    abstract TVAboutFragment contributeTVAboutFragmentInjector();

    @ServiceScope
    @ContributesAndroidInjector(modules = RecommendationModule.class)
    abstract RecommendationService contributeRecommendationServiceInjector();

    @ServiceScope
    @ContributesAndroidInjector(modules = RecommendationContentProviderModule.class)
    abstract RecommendationContentProvider contributeRecommendationContentProviderInjector();

}

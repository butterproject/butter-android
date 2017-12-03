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

package butter.droid.ui;

import butter.droid.base.ui.ActivityScope;
import butter.droid.base.ui.BaseInjectorModule;
import butter.droid.base.ui.FragmentScope;
import butter.droid.ui.about.AboutActivity;
import butter.droid.ui.about.AboutActivityModule;
import butter.droid.ui.about.AboutFragment;
import butter.droid.ui.about.AboutModule;
import butter.droid.ui.beam.BeamPlayerActivity;
import butter.droid.ui.beam.BeamPlayerActivityModule;
import butter.droid.ui.beam.fragment.dialog.BeamDeviceSelectorDialogFragment;
import butter.droid.ui.beam.fragment.dialog.BeamDeviceSelectorDialogModule;
import butter.droid.ui.loading.StreamLoadingActivity;
import butter.droid.ui.loading.StreamLoadingModule;
import butter.droid.ui.loading.fragment.StreamLoadingFragment;
import butter.droid.ui.loading.fragment.StreamLoadingFragmentModule;
import butter.droid.ui.main.MainActivity;
import butter.droid.ui.main.MainModule;
import butter.droid.ui.media.detail.MediaDetailActivity;
import butter.droid.ui.media.detail.MediaDetailModule;
import butter.droid.ui.media.detail.dialog.EpisodeDialogFragment;
import butter.droid.ui.media.detail.dialog.EpisodeDialogModule;
import butter.droid.ui.player.VideoPlayerActivity;
import butter.droid.ui.player.VideoPlayerModule;
import butter.droid.ui.preferences.PreferencesActivity;
import butter.droid.ui.preferences.PreferencesModule;
import butter.droid.ui.search.SearchActivity;
import butter.droid.ui.search.SearchActivityModule;
import butter.droid.ui.search.SearchFragment;
import butter.droid.ui.search.SearchModule;
import butter.droid.ui.terms.TermsActivity;
import butter.droid.ui.terms.TermsModule;
import butter.droid.ui.trailer.TrailerPlayerActivity;
import butter.droid.ui.trailer.TrailerPlayerActivityModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(includes = {
        BaseInjectorModule.class
})
public abstract class InjectorModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = PreferencesModule.class)
    abstract PreferencesActivity contributePreferencesActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = StreamLoadingModule.class)
    abstract StreamLoadingActivity contributeStreamLoadingActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TermsModule.class)
    abstract TermsActivity contributeTermsActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = BeamPlayerActivityModule.class)
    abstract BeamPlayerActivity contributeBeamPlayerActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = MainModule.class)
    abstract MainActivity contributeMainActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = MediaDetailModule.class)
    abstract MediaDetailActivity contributeMediaDetailActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = VideoPlayerModule.class)
    abstract VideoPlayerActivity contributeVideoPlayerActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = AboutActivityModule.class)
    abstract AboutActivity contributeAboutActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = SearchActivityModule.class)
    abstract SearchActivity contributeSearchActivityInjector();

    @ActivityScope
    @ContributesAndroidInjector(modules = TrailerPlayerActivityModule.class)
    abstract TrailerPlayerActivity contributeTrailerPlayerActivityInjector();

    @FragmentScope
    @ContributesAndroidInjector(modules = AboutModule.class)
    abstract AboutFragment contributeAboutFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector(modules = SearchModule.class)
    abstract SearchFragment contributeSearchFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector(modules = StreamLoadingFragmentModule.class)
    abstract StreamLoadingFragment contributeStreamLoadingFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector(modules = EpisodeDialogModule.class)
    abstract EpisodeDialogFragment contributeEpisodeDialogFragmentInjector();

    @FragmentScope
    @ContributesAndroidInjector(modules = BeamDeviceSelectorDialogModule.class)
    abstract BeamDeviceSelectorDialogFragment contributeBeamDeviceSelectorDialogFragmentInjector();

}

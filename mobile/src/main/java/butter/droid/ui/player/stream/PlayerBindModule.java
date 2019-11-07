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

package butter.droid.ui.player.stream;

import butter.droid.base.ui.SubFragmentScope;
import butter.droid.ui.media.detail.dialog.subs.SubsPickerDialog;
import butter.droid.ui.media.detail.dialog.subs.SubsPickerModule;
import butter.droid.ui.media.detail.dialog.subs.SubsPickerParent;
import butter.droid.ui.player.abs.AbsPlayerPresenter;
import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public interface PlayerBindModule {

    @Binds PlayerPresenter bindPresenter(PlayerPresenterImpl presenter);

    @Binds AbsPlayerPresenter bindBasePresenter(PlayerPresenter presenter);

    @Binds PlayerView bindView(PlayerFragment fragment);

    @Binds SubsPickerParent subsPickerParent(PlayerPresenterImpl presenter);

    @SubFragmentScope
    @ContributesAndroidInjector(modules = SubsPickerModule.class)
    SubsPickerDialog contributeSubsPickerDialogInjector();

}

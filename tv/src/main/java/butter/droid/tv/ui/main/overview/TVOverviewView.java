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

package butter.droid.tv.ui.main.overview;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import butter.droid.base.manager.internal.provider.model.ProviderWrapper;
import butter.droid.provider.base.filter.Filter;
import butter.droid.provider.base.nav.NavItem;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;

public interface TVOverviewView {

    void displayProviderData(int providerId, List<MediaCardItem> list);

    void updateBackgroundImage(String url);

    void showErrorMessage(@StringRes int message);

    void openPreferencesScreen();

    void openMediaActivity(@StringRes int title, final int providerId, @NonNull Filter filter);

    void setupProviderRows(ProviderWrapper[] providers);

    void setupMoreRow();

    void displayProviderSorters(int providerId, List<NavItem> value);
}

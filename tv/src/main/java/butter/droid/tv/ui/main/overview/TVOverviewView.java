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

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import butter.droid.base.providers.media.MediaProvider.NavInfo;
import butter.droid.base.torrent.StreamInfo;
import butter.droid.provider.base.Movie;
import butter.droid.tv.presenters.MediaCardPresenter.MediaCardItem;
import java.util.List;

public interface TVOverviewView {

    void displayMovies(final List<MediaCardItem> list);

    void updateBackgroundImage(String url);

    void showErrorMessage(@StringRes int message);

    void showErrorMessage(String message);

    void openTestPlayerPicker();

    void openPreferencesScreen();

    void openMediaActivity(@NonNull NavInfo navInfo);

    void setupMoviesRow();

    void setupTVShowsRow();

    void setupMoreMoviesRow(List<NavInfo> navigation);

    void setupMoreTVShowsRow(List<NavInfo> navigation);

    void setupMoreRow();

    void showCustomDebugUrl();

    void startTrailerScreen(final Movie movie, final String location);

    void startPlayerActivity(StreamInfo streamInfo);
}

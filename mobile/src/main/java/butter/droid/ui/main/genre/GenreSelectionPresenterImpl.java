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

package butter.droid.ui.main.genre;

import java.util.ArrayList;
import java.util.List;

import butter.droid.base.manager.provider.ProviderManager;
import butter.droid.base.providers.media.models.Genre;
import butter.droid.ui.main.MainPresenter;
import butter.droid.ui.main.genre.list.model.UiGenre;

public class GenreSelectionPresenterImpl implements GenreSelectionPresenter {

    private final GenreSelectionView view;
    private final ProviderManager providerManager;
    private final MainPresenter parentPresenter;

    private List<UiGenre> genres;
    private int selectedGenrePosition = -1;

    public GenreSelectionPresenterImpl(GenreSelectionView view, ProviderManager providerManager,
            MainPresenter parentPresenter) {
        this.view = view;
        this.providerManager = providerManager;
        this.parentPresenter = parentPresenter;
    }

    @Override public void onViewCreated() {

        List<Genre> genreList = providerManager.getCurrentMediaProvider().getGenres();
        genres = mapGenres(genreList);

        view.displayGenres(genres);
        onGenreSelected(0);
    }

    @Override public void onGenreSelected(int position) {
        if (position != selectedGenrePosition) {
            if (selectedGenrePosition >= 0) {
                genres.get(selectedGenrePosition).setSelected(false);
                view.notifyItemUpdated(selectedGenrePosition);
            }

            UiGenre genre = genres.get(position);
            genre.setSelected(true);
            selectedGenrePosition = position;
            view.notifyItemUpdated(position);

            parentPresenter.onGenreChanged(genre);
        }
    }

    private List<UiGenre> mapGenres(List<Genre> genres) {

        List<UiGenre> uiGenres = new ArrayList<>();

        if (genres != null && genres.size() > 0) {
            for (Genre genre : genres) {
                uiGenres.add(new UiGenre(genre.getKey(), genre.getLabelId()));
            }
        }

        return uiGenres;

    }
}

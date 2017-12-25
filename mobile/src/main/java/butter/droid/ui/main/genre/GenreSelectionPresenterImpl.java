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

import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import butter.droid.base.manager.internal.provider.ProviderManager;
import butter.droid.ui.main.MainPresenter;
import butter.droid.ui.main.genre.list.model.UiGenre;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

public class GenreSelectionPresenterImpl implements GenreSelectionPresenter {

    private final GenreSelectionView view;
    private final ProviderManager providerManager;
    private final MainPresenter parentPresenter;

    @Nullable private Disposable genresDisposable;

    private List<UiGenre> genres;
    private int selectedGenrePosition = -1;

    public GenreSelectionPresenterImpl(GenreSelectionView view, ProviderManager providerManager,
            MainPresenter parentPresenter) {
        this.view = view;
        this.providerManager = providerManager;
        this.parentPresenter = parentPresenter;
    }

    @Override public void onViewCreated(final int providerId) {
        disposeGenres();

        providerManager.getMediaProvider(providerId).genres()
                .flatMapObservable(Observable::fromIterable)
                .map(UiGenre::new)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<UiGenre>>() {
                    @Override public void onSubscribe(final Disposable d) {
                        genresDisposable = d;
                    }

                    @Override public void onSuccess(final List<UiGenre> value) {
                        genres = value;
                        view.displayGenres(genres);
                        onGenreSelected(0);
                    }

                    @Override public void onError(final Throwable e) {
                        // TODO: 8/5/17 Show error
                    }
                });
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

    @Override public void onDestroy() {
        disposeGenres();
    }

    @MainThread private void disposeGenres() {
        if (this.genresDisposable != null) {
            this.genresDisposable.dispose();
            this.genresDisposable = null;
        }
    }

}

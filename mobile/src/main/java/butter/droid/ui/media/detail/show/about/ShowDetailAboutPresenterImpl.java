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

package butter.droid.ui.media.detail.show.about;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import butter.droid.provider.base.module.Show;
import butter.droid.provider.base.filter.Genre;

public class ShowDetailAboutPresenterImpl implements ShowDetailAboutPresenter {

    private final ShowDetailAboutView view;
    private final Context context;

    private Show show;

    public ShowDetailAboutPresenterImpl(ShowDetailAboutView view, Context context) {
        this.view = view;
        this.context = context;
    }

    @Override public void onCreate(Show show) {

        if (show == null) {
            throw new IllegalStateException("Show can not be null");
        }

        this.show = show;

    }

    @Override public void readMoreClicked() {
        view.openSynopsisDialog(show.getSynopsis());
    }

    @Override public void onViewCreated() {
        displayShowData(show);
    }

    private void displayShowData(@NonNull Show show) {
        view.displayTitle(show.getTitle());
        displayRating(show.getRating());
        displayMetaData(show);
        displaySynopsis(show.getSynopsis());
        view.displayImage(show.getPoster());
    }

    private void displayRating(@Nullable Float rating) {
        if (rating != null) {
            int ratingInt = (int) (rating * 10);
            String cd = "Rating: " + ratingInt + " out of 10";
            view.displayRating(ratingInt, cd);
        } else {
            view.hideRating();
        }
    }

    private void displayMetaData(@NonNull Show show) {
        StringBuilder metaDataStr = new StringBuilder();
        metaDataStr.append(show.getYear());

//        if (show.status != Show.Status.UNKNOWN) {
//            metaDataStr.append(" • ");
//            if (show.status == Show.Status.CONTINUING) {
//                metaDataStr.append(context.getString(R.string.continuing));
//            } else {
//                metaDataStr.append(context.getString(R.string.ended));
//            }
//        }

        Genre[] genres = show.getGenres();
        if (genres != null && genres.length > 0) {
            metaDataStr.append(" • ");
            metaDataStr.append(genres[0].getName());
        }

        view.displayMetaData(metaDataStr.toString());

    }

    private void displaySynopsis(@Nullable String synopsis) {
        if (!TextUtils.isEmpty(synopsis)) {
            view.displaySynopsis(synopsis);
        } else {
            view.hideSynopsis();
        }
    }
}

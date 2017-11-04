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

package butter.droid.provider.base.filter

import android.support.annotation.StringRes
import butter.droid.provider.base.R
import org.parceler.Parcel
import org.parceler.ParcelConstructor

@Parcel(Parcel.Serialization.BEAN)
data class Genre @ParcelConstructor constructor(val key: String, @StringRes val name: Int) {

    companion object {
        @JvmField val ACTION = Genre("action", R.string.genre_action)
        @JvmField val ADVENTURE = Genre("adventure", R.string.genre_adventure)
        @JvmField val ANIMATION = Genre("animation", R.string.genre_animation)
        @JvmField val BIOGRAPHY = Genre("biography", R.string.genre_biography)
        @JvmField val COMEDY = Genre("comedy", R.string.genre_comedy)
        @JvmField val CRIME = Genre("crime", R.string.genre_crime)
        @JvmField val DOCUMENTARY = Genre("documentary", R.string.genre_documentary)
        @JvmField val DRAMA = Genre("drama", R.string.genre_drama)
        @JvmField val FAMILY = Genre("family", R.string.genre_family)
        @JvmField val FANTASY = Genre("fantasy", R.string.genre_fantasy)
        @JvmField val FILM_NOIR = Genre("film-noir", R.string.genre_film_noir)
        @JvmField val HISTORY = Genre("history", R.string.genre_history)
        @JvmField val HORROR = Genre("horror", R.string.genre_horror)
        @JvmField val MUSIC = Genre("music", R.string.genre_music)
        @JvmField val MUSICAL = Genre("musical", R.string.genre_musical)
        @JvmField val MYSTERY = Genre("mystery", R.string.genre_mystery)
        @JvmField val ROMANCE = Genre("romance", R.string.genre_romance)
        @JvmField val SCI_FI = Genre("sci-fi", R.string.genre_sci_fi)
        @JvmField val SPORT = Genre("sport", R.string.genre_sport)
        @JvmField val THRILLER = Genre("thriller", R.string.genre_thriller)
        @JvmField val WAR = Genre("war", R.string.genre_war)
        @JvmField val WESTERN = Genre("western", R.string.genre_western)
    }

}

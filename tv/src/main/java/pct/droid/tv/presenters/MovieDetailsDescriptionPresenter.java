/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.tv.presenters;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;


public class MovieDetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
	@Override
	protected void onBindDescription(ViewHolder viewHolder, Object item) {
		Media itemWrapper = (Media) item;
		if (itemWrapper != null && itemWrapper != null) {
			viewHolder.getTitle().setText(itemWrapper.title);
			viewHolder.getSubtitle().setText(itemWrapper.genre);

			if (itemWrapper instanceof Movie) {
				Movie movieItem = (Movie) itemWrapper;
				viewHolder.getBody().setText(movieItem.synopsis);
			}
		}
	}
}

package pct.droid.tv.presenters;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.tv.fragments.PTVMovieDetailsFragment;


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

package pct.droid.tv.presenters;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import pct.droid.base.providers.media.types.Movie;
import pct.droid.tv.fragments.PTVMovieDetailsFragment;


public class MovieDetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
	@Override
	protected void onBindDescription(ViewHolder viewHolder, Object item) {
		PTVMovieDetailsFragment.ItemWrapper itemWrapper = (PTVMovieDetailsFragment.ItemWrapper) item;
		if (itemWrapper != null && itemWrapper.getMedia() != null) {
			viewHolder.getTitle().setText(itemWrapper.getMedia().title);
			viewHolder.getSubtitle().setText(itemWrapper.getMedia().genre);

			if (!itemWrapper.isLoadingDetail() && itemWrapper.getMedia() instanceof Movie) {
				Movie movieItem = (Movie) itemWrapper.getMedia();
				viewHolder.getBody().setText(movieItem.synopsis);
			}
		}
	}
}

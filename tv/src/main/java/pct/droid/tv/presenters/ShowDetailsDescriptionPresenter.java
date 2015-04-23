package pct.droid.tv.presenters;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Movie;
import pct.droid.base.providers.media.models.Show;


public class ShowDetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
	@Override
	protected void onBindDescription(ViewHolder viewHolder, Object item) {
			if (!(item instanceof Show))return;

		Show show = (Show) item;
		if (show != null && show != null) {
			viewHolder.getTitle().setText(show.title);
			viewHolder.getSubtitle().setText(show.genre);
			viewHolder.getBody().setText(show.synopsis);
//			viewHolder.getBody().setText(show.);
		}
	}
}

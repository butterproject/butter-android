package pct.droid.tv.presenters.showdetail;

import android.support.v17.leanback.widget.DetailsOverviewRow;

import pct.droid.base.providers.media.models.Episode;

public class SeasonEpisodeRow extends DetailsOverviewRow {
   private Episode mEpisode;

   public SeasonEpisodeRow(Object episode) {
      super(episode);
      if (episode instanceof Episode)
      this.mEpisode = (Episode) episode;
   }

   public Episode getEpisode() {
      return this.mEpisode;
   }
}

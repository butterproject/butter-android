package pct.droid.tv.presenters.showdetail;

import android.content.Context;
import android.content.Intent;
import android.support.v17.leanback.widget.Row;

import pct.droid.base.providers.media.models.Episode;

public class EpisodeRow extends Row {
   private final Context mContext;
   private final Episode mEpisode;
   private final int mPosition;
   private boolean mRadioClicked;
   private Intent mRadioIntent;


   public EpisodeRow(Context context, int position, Episode episode) {
      this.mRadioClicked = false;
      this.mContext = context;
      this.mPosition = position;
      this.mEpisode = episode;
   }

   public Episode getEpisode() {
      return this.mEpisode;
   }

   public int getPosition() {
      return this.mPosition;
   }



   void onClicked() {
//      if(this.mIntent != null) {
//         this.mContext.startActivity(this.mIntent);
//      }

   }

}

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package pct.droid.tv.presenters.showdetail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import pct.droid.base.providers.media.models.Episode;
import pct.droid.tv.R;

public class EpisodeCardPresenter extends Presenter {

	private Context mContext;
	private int mCardWidth;
	private int mCardHeight;
    private Listener clickListener;

    public EpisodeCardPresenter(Context context) {
		mCardWidth = (int) context.getResources().getDimension(R.dimen.card_height);
		mCardHeight = (int) context.getResources().getDimension(R.dimen.card_width);
	}

    public void setOnClickListener(@NonNull Listener listener) {
        clickListener = listener;
    }

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		mContext = parent.getContext();

		ImageCardView cardView = new ImageCardView(mContext);
		cardView.setCardType(BaseCardView.CARD_TYPE_INFO_UNDER);
		cardView.setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ALWAYS);
		cardView.setFocusable(true);
		cardView.setFocusableInTouchMode(true);
		cardView.setBackgroundColor(ActivityCompat.getColor(mContext, R.color.default_background));

		return new ViewHolder(cardView);
	}

	@Override
	public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
		final Episode episode = (Episode) item;
		((ViewHolder) viewHolder).setEpisode(episode);
		((ViewHolder) viewHolder).getCardView().setTitleText(episode.title);
		((ViewHolder) viewHolder).getCardView().setContentText(
            String.format(mContext.getString(R.string.episode_number_format), episode.episode));
		((ViewHolder) viewHolder).getCardView().setMainImageDimensions(mCardWidth, mCardHeight);
		((ViewHolder) viewHolder).updateCardViewImage(episode.image);
        ((ViewHolder) viewHolder).cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != clickListener) clickListener.onEpisodeClicked(episode);
            }
        });
	}

	@Override
	public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) { }

	public class ViewHolder extends Presenter.ViewHolder {

		private Episode episode;
		private ImageCardView cardView;
		private Drawable defaultImage;
		private PicassoImageCardViewTarget imageCardViewTarget;

		public ViewHolder(View view) {
			super(view);
			cardView = (ImageCardView) view;
			imageCardViewTarget = new PicassoImageCardViewTarget(cardView);
			defaultImage = ActivityCompat.getDrawable(mContext, R.drawable.banner);
            cardView.setMainImage(defaultImage);
		}

		public void setEpisode(Episode episode) {
			this.episode = episode;
		}

		public Episode getEpisode() {
			return episode;
		}

		public ImageCardView getCardView() {
			return cardView;
		}

		protected void updateCardViewImage(String uri) {
			Picasso.with(mContext)
				.load(uri)
				.resize(mCardWidth, mCardHeight)
                .centerCrop()
				.placeholder(defaultImage)
				.error(defaultImage)
				.into(imageCardViewTarget);
		}
	}

	public class PicassoImageCardViewTarget implements Target {
		private ImageCardView mImageCardView;

		public PicassoImageCardViewTarget(ImageCardView imageCardView) {
			mImageCardView = imageCardView;
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
			Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
			mImageCardView.setMainImage(bitmapDrawable);
		}

		@Override
		public void onBitmapFailed(Drawable drawable) {
			mImageCardView.setMainImage(drawable);
		}

		@Override
		public void onPrepareLoad(Drawable drawable) {
			// Do nothing, default_background manager has its own transitions
		}
	}

    public interface Listener {
        void onEpisodeClicked(Episode row);
    }
}

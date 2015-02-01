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

package pct.droid.tv.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import pct.droid.base.providers.media.models.Media;
import pct.droid.tv.R;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class OverviewCardPresenter extends Presenter {

	private static Context mContext;
	private static int mCardWidth;
	private static int mCardHeight;

	private final int mDefaultInfoBackgroundColor;
	private final int mDefaultSelectedInfoBackgroundColor;

	public OverviewCardPresenter(Context context) {
		mDefaultSelectedInfoBackgroundColor = context.getResources().getColor(R.color.primary_dark);
		mDefaultInfoBackgroundColor = context.getResources().getColor(R.color.default_background);
		mCardWidth = (int) context.getResources().getDimension(R.dimen.card_width);
		mCardHeight = (int) context.getResources().getDimension(R.dimen.card_height);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		mContext = parent.getContext();

		final CustomImageCardView cardView = new CustomImageCardView(mContext) {
			@Override
			public void setSelected(boolean selected) {
				if (getCustomSelectedSwatch() != null && selected) {
					setInfoAreaBackgroundColor(getCustomSelectedSwatch().getRgb());
				} else setInfoAreaBackgroundColor(selected ? mDefaultSelectedInfoBackgroundColor : mDefaultInfoBackgroundColor);
				super.setSelected(selected);
			}
		};

		cardView.setFocusable(true);
		cardView.setFocusableInTouchMode(true);
		return new ViewHolder(cardView);
	}

	@Override
	public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object object) {
		OverviewCardItem overview = (OverviewCardItem) object;

		if (overview.isLoading()) onBindLoadingViewHolder(viewHolder, overview);
		else onBindMediaViewHolder(viewHolder, overview);
	}

	public void onBindLoadingViewHolder(Presenter.ViewHolder viewHolder, OverviewCardItem overview) {
		final CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;
		cardView.setMainImageScaleType(ImageView.ScaleType.CENTER_INSIDE);
		cardView.setMainImage(mContext.getResources().getDrawable(R.drawable.placeholder_inset));
		cardView.setTitleText(mContext.getString(R.string.loading));
		cardView.setMainImageDimensions(mCardWidth, mCardHeight);
	}

	public void onBindMediaViewHolder(Presenter.ViewHolder viewHolder, OverviewCardItem overview) {

		Media item = overview.getMedia();
		final CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;
		if (item.image != null) {
			cardView.setTitleText(item.title);
			cardView.setContentText(!TextUtils.isEmpty(item.genre) ? item.genre : item.year);
			cardView.setMainImageDimensions(mCardWidth, mCardHeight);

			Target target = new Target() {
				@Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
					cardView.getMainImageView().setImageBitmap(bitmap);
					Palette.generateAsync(bitmap, 16, new Palette.PaletteAsyncListener() {
						@Override public void onGenerated(Palette palette) {
							Palette.Swatch swatch = palette.getDarkMutedSwatch();
							cardView.setCustomSelectedSwatch(swatch);
						}
					});
				}

				@Override public void onBitmapFailed(Drawable errorDrawable) {

				}

				@Override public void onPrepareLoad(Drawable placeHolderDrawable) {

				}
			};
			//load image
			Picasso.with(mContext).load(item.image).resize(mCardWidth, mCardHeight).centerCrop().into
					(target);
			cardView.setTarget(target);
		}
	}

	@Override
	public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
		CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;
		// Remove references to images so that the garbage collector can free up memory
		cardView.setBadgeImage(null);
		cardView.setMainImage(null);
		if (cardView.getTarget() != null) {
			Picasso.with(mContext).cancelRequest(cardView.getTarget());
			cardView.setTarget(null);
		}
	}


	public static class CustomImageCardView extends ImageCardView {

		private Palette.Swatch mCustomSelectedSwatch;

		private Target mTarget;

		public CustomImageCardView(Context context) {
			super(context);
		}

		public CustomImageCardView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public CustomImageCardView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public Palette.Swatch getCustomSelectedSwatch() {
			return mCustomSelectedSwatch;
		}

		public void setCustomSelectedSwatch(Palette.Swatch customSelectedSwatch) {
			mCustomSelectedSwatch = customSelectedSwatch;
		}

		public Target getTarget() {
			return mTarget;
		}

		public void setTarget(Target target) {
			mTarget = target;
		}
	}

	public static class OverviewCardItem {
		private Media mMedia;
		private boolean mLoading;

		public OverviewCardItem(Media media) {
			mMedia = media;
		}

		public OverviewCardItem(boolean loading) {
			mLoading = loading;
		}

		public Media getMedia() {
			return mMedia;
		}

		public boolean isLoading() {
			return mLoading;
		}
	}


	public static List<OverviewCardItem> convertMediaToOverview(List<Media> items) {
		List<OverviewCardPresenter.OverviewCardItem> list = new ArrayList<>();
		for (Media media : items) list.add(new OverviewCardPresenter.OverviewCardItem(media));
		return list;
	}
}

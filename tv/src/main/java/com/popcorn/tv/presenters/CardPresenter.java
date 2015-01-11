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

package com.popcorn.tv.presenters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.ViewGroup;

import com.popcorn.tv.R;
import com.squareup.picasso.Picasso;

import pct.droid.base.providers.media.types.Media;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {

	private static Context mContext;
	private static int CARD_WIDTH = 313;
	private static int CARD_HEIGHT = 176;
	private Drawable mDefaultCardImage;

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent) {
		mContext = parent.getContext();

		mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.movie);

		ImageCardView cardView = new ImageCardView(mContext) {
			@Override
			public void setSelected(boolean selected) {
				int selected_background = mContext.getResources().getColor(R.color.detail_background);
				int default_background = mContext.getResources().getColor(R.color.default_background);
				int color = selected ? selected_background : default_background;
				findViewById(R.id.info_field).setBackgroundColor(color);
				super.setSelected(selected);
			}
		};

		cardView.setFocusable(true);
		cardView.setFocusableInTouchMode(true);
		return new ViewHolder(cardView);
	}

	@Override
	public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
		Media movie = (Media) item;
		ImageCardView cardView = (ImageCardView) viewHolder.view;

		if (movie.image != null) {
			cardView.setTitleText(movie.title);
			cardView.setContentText(movie.genre);
			cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
			//load image
			Picasso.with(mContext).load(movie.image).resize(CARD_WIDTH,CARD_HEIGHT).centerCrop().error(mDefaultCardImage).into
					(cardView
					.getMainImageView());
		}
	}

	@Override
	public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
		ImageCardView cardView = (ImageCardView) viewHolder.view;
		// Remove references to images so that the garbage collector can free up memory
		cardView.setBadgeImage(null);
		cardView.setMainImage(null);
	}
}

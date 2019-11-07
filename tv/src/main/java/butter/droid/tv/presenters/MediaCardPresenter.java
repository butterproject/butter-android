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

package butter.droid.tv.presenters;

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.leanback.widget.BaseCardView;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import androidx.palette.graphics.Palette;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import butter.droid.base.manager.internal.glide.GlideApp;
import butter.droid.base.manager.internal.glide.transcode.PaletteBitmap;
import butter.droid.base.manager.internal.glide.transition.PaletteBitmapTransitionOptions;
import butter.droid.base.providers.media.model.MediaMeta;
import butter.droid.base.providers.media.model.MediaWrapper;
import butter.droid.base.utils.AnimUtils;
import butter.droid.provider.base.model.Media;
import butter.droid.tv.R;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class MediaCardPresenter extends Presenter {

    private final Context context;

    private final int cardWidth;
    private final int cardHeight;
    @ColorInt private final int defaultInfoBackgroundColor;
    @ColorInt private final int defaultSelectedInfoBackgroundColor;

    public MediaCardPresenter(Context context) {
        this.context = context;
        defaultSelectedInfoBackgroundColor = context.getResources().getColor(R.color.primary_dark);
        defaultInfoBackgroundColor = context.getResources().getColor(R.color.default_background);
        cardWidth = (int) context.getResources().getDimension(R.dimen.card_width);
        cardHeight = (int) context.getResources().getDimension(R.dimen.card_height);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        final CustomImageCardView cardView = new CustomImageCardView(context) {
            @Override
            public void setSelected(boolean selected) {
                if (getCustomSelectedColor() != MediaMeta.COLOR_NONE && selected) {
                    setInfoAreaBackgroundColor(getCustomSelectedColor());
                } else {
                    setInfoAreaBackgroundColor(selected ? defaultSelectedInfoBackgroundColor : defaultInfoBackgroundColor);
                }
                super.setSelected(selected);
            }
        };

        cardView.setInfoAreaBackgroundColor(defaultInfoBackgroundColor);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object object) {
        onBindMediaViewHolder(viewHolder, (MediaCardItem) object);
    }

    public void onBindMediaViewHolder(Presenter.ViewHolder viewHolder, MediaCardItem overview) {

        final MediaWrapper mediaWrapper = overview.getMediaWrapper();
        final Media item = mediaWrapper.getMedia();
        final CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;

        cardView.setTitleText(item.getTitle());
        // cardView.setContentText(!TextUtils.isEmpty(item.getGenres().length) ? item.getGenres() : item.getYear());
        cardView.setContentText(String.valueOf(item.getYear()));
        cardView.getMainImageView().setAlpha(1f);
        cardView.getMainImageView().setPadding(0, 0, 0, 0);
        cardView.setMainImageDimensions(cardWidth, cardHeight);
        cardView.getMainImageView().setVisibility(View.GONE);
        cardView.setCustomSelectedColor(MediaMeta.COLOR_NONE);

        if (item.getPoster() != null) {
            //load image
            GlideApp.with(context)
                    .as(PaletteBitmap.class)
                    .load(item.getPoster())
                    .transition(PaletteBitmapTransitionOptions.withCrossFade())
                    .into(new ViewTarget<ImageView, PaletteBitmap>(cardView.getMainImageView()) {
                        @Override public void onResourceReady(@NonNull PaletteBitmap resource,
                                @Nullable Transition<? super PaletteBitmap> transition) {
                            Palette palette = resource.getPalette();
                            mediaWrapper.setColor(palette.getVibrantColor(MediaMeta.COLOR_NONE));
                            cardView.setCustomSelectedColor(mediaWrapper.getColor());

                            cardView.getMainImageView().setImageBitmap(resource.getBitmap());
                            cardView.getMainImageView().setVisibility(View.GONE);
                            AnimUtils.fadeIn(cardView.getMainImageView());
                        }
                    });
        } else {
            cardView.getMainImageView().setImageResource(R.drawable.placeholder_inset);
            cardView.getMainImageView().setAlpha(0.4f);
            cardView.getMainImageView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            cardView.getMainImageView().setVisibility(View.GONE);
            AnimUtils.fadeIn(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
        GlideApp.with(context).clear(cardView.getMainImageView());
    }

    public static class CustomImageCardView extends ImageCardView {

        @ColorInt private int customSelectedColor;

        public CustomImageCardView(Context context) {
            super(context);
            setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ALWAYS);
        }

        public CustomImageCardView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ALWAYS);
        }

        public CustomImageCardView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ALWAYS);
        }

        @ColorInt public int getCustomSelectedColor() {
            return customSelectedColor;
        }

        public void setCustomSelectedColor(@ColorInt int customSelectedColor) {
            this.customSelectedColor = customSelectedColor;
        }

    }

    public static class MediaCardItem {

        private final MediaWrapper media;

        public MediaCardItem(final MediaWrapper media) {
            this.media = media;
        }

        public MediaWrapper getMediaWrapper() {
            return media;
        }

    }

    public static List<MediaCardItem> convertMediaToOverview(List<MediaWrapper> items) {
        List<MediaCardItem> list = new ArrayList<>();
        for (MediaWrapper media : items) {
            list.add(new MediaCardItem(media));
        }
        return list;
    }

}

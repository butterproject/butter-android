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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butter.droid.base.utils.AnimUtils;
import butter.droid.provider.base.module.Media;
import butter.droid.tv.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.util.ArrayList;
import java.util.List;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class MediaCardPresenter extends Presenter {

    private final Context context;
    private final Picasso picasso;

    private final int cardWidth;
    private final int cardHeight;
    @ColorInt private final int defaultInfoBackgroundColor;
    @ColorInt private final int defaultSelectedInfoBackgroundColor;

    public MediaCardPresenter(Context context, final Picasso picasso) {
        this.context = context;
        this.picasso = picasso;
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
                if (getCustomSelectedSwatch() != null && selected) {
                    setInfoAreaBackgroundColor(getCustomSelectedSwatch().getRgb());
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

        Media item = overview.getMedia();
        final CustomImageCardView cardView = (CustomImageCardView) viewHolder.view;

        cardView.setTitleText(item.getTitle());
        // cardView.setContentText(!TextUtils.isEmpty(item.getGenres().length) ? item.getGenres() : item.getYear());
        cardView.setContentText(String.valueOf(item.getYear()));
        cardView.getMainImageView().setAlpha(1f);
        cardView.getMainImageView().setPadding(0, 0, 0, 0);
        cardView.setMainImageDimensions(cardWidth, cardHeight);
        cardView.getMainImageView().setVisibility(View.GONE);
        cardView.setCustomSelectedSwatch(null);

        if (item.getPoster() != null) {
            Target target = new Target() {
                @Override public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    Palette.from(bitmap).maximumColorCount(16).generate(new Palette.PaletteAsyncListener() {
                        @Override public void onGenerated(Palette palette) {
                            Palette.Swatch swatch = palette.getDarkMutedSwatch();
                            cardView.setCustomSelectedSwatch(swatch);

                            cardView.getMainImageView().setImageBitmap(bitmap);
                            cardView.getMainImageView().setVisibility(View.GONE);
                            AnimUtils.fadeIn(cardView.getMainImageView());
                        }
                    });
                }

                @Override public void onBitmapFailed(Drawable errorDrawable) {
                    cardView.getMainImageView().setImageResource(R.drawable.placeholder_inset);
                    cardView.getMainImageView().setAlpha(0.4f);
                    cardView.getMainImageView().setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    cardView.getMainImageView().setVisibility(View.GONE);
                    AnimUtils.fadeIn(cardView.getMainImageView());

                }

                @Override public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            //load image
            picasso.load(item.getPoster()).resize(cardWidth, cardHeight).centerCrop().into(target);
            cardView.setTarget(target);
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
        if (cardView.getTarget() != null) {
            picasso.cancelRequest(cardView.getTarget());
            cardView.setTarget(null);
        }
    }

    public static class CustomImageCardView extends ImageCardView {

        private Palette.Swatch customSelectedSwatch;

        private Target target;

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

        public Palette.Swatch getCustomSelectedSwatch() {
            return customSelectedSwatch;
        }

        public void setCustomSelectedSwatch(Palette.Swatch customSelectedSwatch) {
            this.customSelectedSwatch = customSelectedSwatch;
        }

        public Target getTarget() {
            return target;
        }

        public void setTarget(Target target) {
            this.target = target;
        }
    }

    public static class MediaCardItem {

        private final int providerId;
        private final Media media;

        public MediaCardItem(final int providerId, final Media media) {
            this.providerId = providerId;
            this.media = media;
        }

        public int getProviderId() {
            return providerId;
        }

        public Media getMedia() {
            return media;
        }

    }

    public static List<MediaCardItem> convertMediaToOverview(int providerId, List<Media> items) {
        List<MediaCardItem> list = new ArrayList<>();
        for (Media media : items) {
            list.add(new MediaCardItem(providerId, media));
        }
        return list;
    }

}

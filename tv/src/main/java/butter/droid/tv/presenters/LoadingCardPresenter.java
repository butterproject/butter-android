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
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import androidx.core.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.ImageView;
import butter.droid.tv.R;

public class LoadingCardPresenter extends Presenter {

    private final Context context;

    private final int cardWidth;
    private final int cardHeight;
    @ColorInt private final int defaultInfoBackgroundColor;

    public LoadingCardPresenter(final Context context) {
        this.context = context;

        cardWidth = (int) context.getResources().getDimension(R.dimen.card_width);
        cardHeight = (int) context.getResources().getDimension(R.dimen.card_height);
        defaultInfoBackgroundColor = ContextCompat.getColor(context, R.color.default_background);
    }

    @Override public ViewHolder onCreateViewHolder(final ViewGroup parent) {
        final ImageCardView cardView = new ImageCardView(context);
        cardView.setInfoAreaBackgroundColor(defaultInfoBackgroundColor);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        return new ViewHolder(cardView);
    }

    @Override public void onBindViewHolder(final ViewHolder viewHolder, final Object item) {
        onBindLoadingViewHolder(viewHolder);
    }

    @Override public void onUnbindViewHolder(final ViewHolder viewHolder) {
        // nothing to do
    }

    public void onBindLoadingViewHolder(Presenter.ViewHolder viewHolder) {
        final ImageCardView cardView = (ImageCardView) viewHolder.view;
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER_INSIDE);
        cardView.setMainImage(ContextCompat.getDrawable(context, R.drawable.placeholder_inset));
        cardView.setTitleText(context.getString(R.string.loading));
        cardView.setMainImageDimensions(cardWidth, cardHeight);
    }

    public static class LoadingCardItem {

    }

}

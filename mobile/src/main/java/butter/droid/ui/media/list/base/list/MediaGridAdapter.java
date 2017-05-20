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

package butter.droid.ui.media.list.base.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.List;

import butter.droid.R;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.PixelUtils;
import butter.droid.manager.paging.PagingAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MediaGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PagingAdapter<Media> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_PROGRESS = 1;

    private int itemWidth;
    private int itemHeight;
    private int margin;
    private int columns;

    private List<Media> items;
    private boolean showLoading = true;

    public MediaGridAdapter(Context context, Integer columns) {
        this.columns = columns;

        int screenWidth = PixelUtils.getScreenWidth(context);
        itemWidth = (screenWidth / columns);
        itemHeight = (int) ((double) itemWidth / 0.677);
        margin = PixelUtils.getPixelsFromDp(context, 2);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_PROGRESS:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_griditem_loading, parent, false);
                return new MediaGridAdapter.LoadingHolder(view);
            case VIEW_TYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_griditem, parent, false);
                return new MediaGridAdapter.ViewHolder(view);
            default:
                throw new IllegalStateException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        int doubleMargin = margin * 2;
        int topMargin = (position < columns) ? margin * 2 : margin;

        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.height = itemHeight;
        layoutParams.width = itemWidth;
        int mod = LocaleUtils.currentLocaleIsRTL() ? 1 : 0;
        if (position % columns == mod) {
            layoutParams.setMargins(doubleMargin, topMargin, margin, margin);
        } else if (position % columns == columns - 1) {
            layoutParams.setMargins(margin, topMargin, doubleMargin, margin);
        } else {
            layoutParams.setMargins(margin, topMargin, margin, margin);
        }
        viewHolder.itemView.setLayoutParams(layoutParams);

        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            final ViewHolder videoViewHolder = (ViewHolder) viewHolder;
            final Media item = getItem(position);

            videoViewHolder.title.setText(item.title);
            videoViewHolder.year.setText(item.year);

            if (item.image != null && !item.image.equals("")) {
                Picasso.with(videoViewHolder.coverImage.getContext()).load(item.image)
                        .resize(itemWidth, itemHeight)
                        .transform(DrawGradient.INSTANCE)
                        .into(videoViewHolder.coverImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (items != null && items.size() > 0) {
            count += items.size();
        }

        if (showLoading()) {
            count++;
        }

        return count;
    }

    @Override public void addItems(@Nullable List<Media> items) {
        if (this.items == null) {
            this.items = items;

            if (items != null && items.size() > 0) {
                int size = items.size();
                notifyItemRangeInserted(0, size);

                if (showLoading) {
                    notifyItemInserted(size);
                }
            }
        } else if (items != null) {
            int lastItem = this.items.size();
            this.items.addAll(items);
            int size = items.size();
            notifyItemRangeInserted(lastItem, size);

            if (lastItem == 0 && showLoading) {
                notifyItemInserted(size);
            }
        }

    }

    @Override public void clear() {
        if (items != null && !items.isEmpty()) {
            notifyItemRangeRemoved(0, items.size());
            items = null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getItemsSize()) {
            return VIEW_TYPE_ITEM;
        } else {
            return VIEW_TYPE_PROGRESS;
        }
    }

    @Nullable public Media getItem(int position) {
        if (position < getItemsSize()) {
            return items.get(position);
        } else {
            return null;
        }
    }

    @Override public void showLoading(boolean show) {
        if (showLoading != show) {
            boolean before = showLoading();
            showLoading = show;
            boolean after = showLoading();

            if (before != after) {
                if (after) {
                    notifyItemInserted(getItemsSize());
                } else {
                    notifyItemRemoved(getItemsSize());
                }
            }
        }
    }

    private boolean showLoading() {
        return showLoading && items != null && items.size() > 0;
    }

    private int getItemsSize() {
        if (items != null) {
            return items.size();
        } else {
            return 0;
        }
    }

    private class LoadingHolder extends RecyclerView.ViewHolder {

        View itemView;

        public LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setMinimumHeight(itemHeight);
        }

    }

    private static class DrawGradient implements Transformation {
        static Transformation INSTANCE = new DrawGradient();

        @Override
        public Bitmap transform(Bitmap src) {
            // Code borrowed from https://stackoverflow.com/questions/23657811/how-to-mask-bitmap-with-lineargradient-shader-properly
            int width = src.getWidth();
            int height = src.getHeight();
            Bitmap overlay = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(overlay);

            canvas.drawBitmap(src, 0, 0, null);
            src.recycle();

            Paint paint = new Paint();
            float gradientHeight = height / 2f;
            LinearGradient shader = new LinearGradient(0, height - gradientHeight, 0, height, 0xFFFFFFFF, 0x00FFFFFF,
                    Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawRect(0, height - gradientHeight, width, height, paint);
            return overlay;
        }

        @Override
        public String key() {
            return "gradient()";
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        @BindView(R.id.focus_overlay) View focusOverlay;
        @BindView(R.id.cover_image) ImageView coverImage;
        @BindView(R.id.title) TextView title;
        @BindView(R.id.year) TextView year;

        private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                focusOverlay.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
            coverImage.setMinimumHeight(itemHeight);

            itemView.setOnFocusChangeListener(onFocusChangeListener);
        }

        public ImageView getCoverImage() {
            return coverImage;
        }

    }
}

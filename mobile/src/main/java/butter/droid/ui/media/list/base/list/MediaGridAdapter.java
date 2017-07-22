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
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butter.droid.R;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.PixelUtils;
import butter.droid.base.manager.internal.paging.PagingAdapter;
import butter.droid.provider.base.module.Media;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import java.util.List;

public class MediaGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PagingAdapter<Media> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_PROGRESS = 1;

    private List<Media> items;

    private final int itemHeight;
    private final int itemWidth;

    private boolean showLoading = true;

    public MediaGridAdapter(final int itemHeight, final int itemWidth) {
        this.itemHeight = itemHeight;
        this.itemWidth = itemWidth;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        switch (viewType) {
            case VIEW_TYPE_PROGRESS:
                view = inflater.inflate(R.layout.media_griditem_loading, parent, false);
                return new LoadingHolder(view, itemHeight);
            case VIEW_TYPE_ITEM:
                view = inflater.inflate(R.layout.media_griditem, parent, false);
                return new ViewHolder(view, itemHeight);
            default:
                throw new IllegalStateException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            final ViewHolder videoViewHolder = (ViewHolder) viewHolder;
            final Media item = getItem(position);

            videoViewHolder.title.setText(item.getTitle());
            videoViewHolder.year.setText(String.valueOf(item.getYear()));

            if (!TextUtils.isEmpty(item.getPoster())) {
                final Context context = videoViewHolder.coverImage.getContext();
                Picasso.with(context).load(item.getPoster()).resize(itemWidth, itemHeight).transform(DrawGradient.INSTANCE)
                        .into(videoViewHolder.coverImage);
            }
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

    @Override
    public void addItems(@Nullable List<Media> items) {
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

    @Nullable
    public Media getItem(int position) {
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

    public static class MediaGridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private final int itemWidth;
        private final int itemHeight;
        private final int margin;
        private final int doubleMargin;
        private final int columns;
        private final int mod;

        public MediaGridSpacingItemDecoration(final Context context, final int columns) {
            this.columns = columns;
            final int screenWidth = PixelUtils.getScreenWidth(context);
            this.itemWidth = (screenWidth / columns);
            this.itemHeight = (int) ((double) itemWidth / 0.677);
            this.margin = PixelUtils.getPixelsFromDp(context, 2);
            this.doubleMargin = margin * 2;
            this.mod = LocaleUtils.currentLocaleIsRTL() ? 1 : 0;
        }

        @Override
        public void getItemOffsets(final Rect outRect, final View view, final RecyclerView parent, final RecyclerView.State state) {
            final GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
            layoutParams.height = itemHeight;
            view.setLayoutParams(layoutParams);

            final int position = parent.getChildAdapterPosition(view);
            final int topMargin = (position < columns) ? doubleMargin : margin;

            if (position % columns == mod) {
                outRect.set(doubleMargin, topMargin, margin, margin);
            } else if (position % columns == columns - 1) {
                outRect.set(margin, topMargin, doubleMargin, margin);
            } else {
                outRect.set(margin, topMargin, margin, margin);
            }
        }

        public int getItemHeight() {
            return itemHeight;
        }

        public int getItemWidth() {
            return itemWidth;
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

    private static class LoadingHolder extends RecyclerView.ViewHolder {

        LoadingHolder(View itemView, int itemHeight) {
            super(itemView);
            itemView.setMinimumHeight(itemHeight);
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

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

        public ViewHolder(View itemView, int itemHeight) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            coverImage.setMinimumHeight(itemHeight);
            itemView.setOnFocusChangeListener(onFocusChangeListener);
        }

        public ImageView getCoverImage() {
            return coverImage;
        }

    }
}

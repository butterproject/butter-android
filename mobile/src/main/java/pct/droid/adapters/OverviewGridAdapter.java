package pct.droid.adapters;

import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.base.PopcornApplication;
import pct.droid.base.providers.media.types.Media;
import pct.droid.base.utils.AnimUtils;
import pct.droid.base.utils.PixelUtils;


public class OverviewGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int mItemWidth, mItemHeight, mMargin, mColumns;
    private ArrayList<OverviewItem> mItems;
    private ArrayList<Media> mData;
    private OverviewGridAdapter.OnItemClickListener mItemClickListener;
    final int NORMAL = 0, LOADING = 1;

    public OverviewGridAdapter(Activity activity, ArrayList<Media> items, Integer columns) {
        mColumns = columns;

        int screenWidth = PixelUtils.getScreenWidth(activity);
        mItemWidth = (screenWidth / columns);
        mItemHeight = (int) ((double) mItemWidth / 0.677);
        mMargin = PixelUtils.getPixelsFromDp(activity, 2);

        setItems(items);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case LOADING:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_overview_griditem_loading, parent, false);
                return new OverviewGridAdapter.LoadingHolder(v);
            case NORMAL:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_overview_griditem, parent, false);
                return new OverviewGridAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        int double_margin = mMargin * 2;
        int top_margin = (position < mColumns) ? mMargin * 2 : mMargin;

        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.height = mItemHeight;
        layoutParams.width = mItemWidth;
        if (position % mColumns == 0) {
            layoutParams.setMargins(double_margin, top_margin, mMargin, mMargin);
        } else if (position % mColumns == mColumns - 1) {
            layoutParams.setMargins(mMargin, top_margin, double_margin, mMargin);
        } else {
            layoutParams.setMargins(mMargin, top_margin, mMargin, mMargin);
        }
        viewHolder.itemView.setLayoutParams(layoutParams);

        if (getItemViewType(position) == NORMAL) {
            final ViewHolder videoViewHolder = (ViewHolder) viewHolder;
            final OverviewItem overviewItem = getItem(position);
            Media item = overviewItem.media;

            videoViewHolder.title.setVisibility(View.GONE);
            videoViewHolder.title.setText(item.title.toUpperCase(Locale.getDefault()));
            if(overviewItem.imageError) {
                AnimUtils.fadeIn(videoViewHolder.title);
            }

            if (item.image != null && !item.image.equals("")) {
                PopcornApplication.getPicasso().load(item.image)
                        .resize(mItemWidth, mItemHeight)
                        .into(videoViewHolder.coverImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                overviewItem.imageError = false;
                            }

                            @Override
                            public void onError() {
                                overviewItem.imageError = true;
                                if(((ViewHolder) viewHolder).title.getVisibility() != View.VISIBLE)
                                    AnimUtils.fadeIn(videoViewHolder.title);
                            }
                        });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isLoadingItem) {
            return LOADING;
        }
        return NORMAL;
    }

    public OverviewItem getItem(int position) {
        return mItems.get(position);
    }

    public void setOnItemClickListener(OverviewGridAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void removeLoading() {
        if (getItemCount() <= 0) return;
        OverviewItem item = mItems.get(getItemCount() - 1);
        if (item.isLoadingItem) {
            mItems.remove(getItemCount() - 1);
            notifyDataSetChanged();
        }
    }

    public void addLoading() {
        OverviewItem item = null;
        if (getItemCount() != 0) {
            item = mItems.get(getItemCount() - 1);
        }

        if (getItemCount() == 0 || (item != null && !item.isLoadingItem)) {
            mItems.add(new OverviewItem(true));
            notifyDataSetChanged();
        }
    }

    public ArrayList<Media> getItems() {
        return (ArrayList<Media>)mData.clone();
    }

    public void setItems(ArrayList<Media> items) {
        mData = items;
        mItems = new ArrayList<>();
        for(Media item : items) {
            mItems.add(new OverviewItem(item));
        }
        notifyDataSetChanged();
    }

    public void clearItems() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, Media item, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        @InjectView(R.id.coverImage)
        ImageView coverImage;
        @InjectView(R.id.title)
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
            coverImage.setMinimumHeight(mItemHeight);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                int position = getPosition();
                Media item = getItem(position).media;
                mItemClickListener.onItemClick(view, item, position);
            }
        }

    }

    class LoadingHolder extends RecyclerView.ViewHolder {

        View itemView;

        public LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setMinimumHeight(mItemHeight);
        }

    }

    class OverviewItem {
        Media media;
        boolean imageError = false;
        boolean isLoadingItem = false;

        OverviewItem(Media media) {
            this.media = media;
        }

        OverviewItem(boolean loading) {
            this.isLoadingItem = loading;
        }
    }

}

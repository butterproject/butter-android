package pct.droid.adapters;

import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.providers.media.MediaProvider;
import pct.droid.providers.media.YTSProvider;
import pct.droid.utils.LogUtils;
import pct.droid.utils.PixelUtils;


public class OverviewGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    int mItemWidth, mItemHeight, mMargin, mColumns;
    ArrayList<MediaProvider.Video> mItems;
    OverviewGridAdapter.OnItemClickListener mItemClickListener;
    final int NORMAL = 0, LOADING = 1;

    public OverviewGridAdapter(Activity activity, ArrayList<MediaProvider.Video> items, Integer columns) {
        mColumns = columns;

        mItemWidth = (activity.getWindow().getDecorView().getWidth() / columns);
        mItemHeight = (int) (1.5 * (double) mItemWidth);
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int double_margin = mMargin * 2;
        int top_margin = (position < mColumns) ? mMargin * 2 : mMargin;

        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        if (position % mColumns == 0) {
            layoutParams.setMargins(double_margin, top_margin, mMargin, mMargin);
        } else if (position % mColumns == mColumns - 1) {
            layoutParams.setMargins(mMargin, top_margin, double_margin, mMargin);
        } else {
            layoutParams.setMargins(mMargin, top_margin, mMargin, mMargin);
        }
        viewHolder.itemView.setLayoutParams(layoutParams);

        if(getItemViewType(position) == NORMAL) {
            ViewHolder videoViewHolder = (ViewHolder) viewHolder;
            MediaProvider.Video item = getItem(position);
            Picasso.with(videoViewHolder.coverImage.getContext()).load(item.image)
                    .resize(mItemWidth, mItemHeight)
                    .into(videoViewHolder.coverImage);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position == getItemCount() - 1) {
            return LOADING;
        }
        return NORMAL;
    }

    public MediaProvider.Video getItem(int position) {
        return mItems.get(position);
    }

    public void setOnItemClickListener(OverviewGridAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public ArrayList<MediaProvider.Video> getItems() {
        ArrayList<MediaProvider.Video> returnData = (ArrayList<MediaProvider.Video>) mItems.clone();
        returnData.remove(getItemCount() - 1);
        return returnData;
    }

    public void setItems(ArrayList<MediaProvider.Video> items) {
        MediaProvider.Video loadingItem = new MediaProvider.Video();
        items.add(loadingItem);
        mItems = items;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, MediaProvider.Video item, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        @InjectView(R.id.coverImage)
        ImageView coverImage;

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
                YTSProvider.Video item = (YTSProvider.Video) getItem(position);
                mItemClickListener.onItemClick(view, item, position);
            }
        }

    }

    public class LoadingHolder extends RecyclerView.ViewHolder {

        View itemView;
        public LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setMinimumHeight(mItemHeight);
        }

    }

}

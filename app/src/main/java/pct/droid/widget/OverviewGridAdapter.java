package pct.droid.widget;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.providers.media.MediaProvider;
import pct.droid.providers.media.YTSProvider;
import pct.droid.utils.LogUtils;
import pct.droid.utils.PixelUtils;


public class OverviewGridAdapter extends RecyclerView.Adapter<OverviewGridAdapter.ViewHolder> {

    Integer mItemWidth, mItemHeight;
    ArrayList<MediaProvider.Video> mItems;
    OverviewGridAdapter.OnItemClickListener mItemClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        @InjectView(R.id.coverImage)
        ImageView coverImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
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

    public interface OnItemClickListener {
        public void onItemClick(View v, MediaProvider.Video item, int position);
    }

    public OverviewGridAdapter(Activity activity, ArrayList<MediaProvider.Video> items, Integer columns) {
        mItems = items;
        mItemWidth = (activity.getWindow().getDecorView().getWidth() / columns);
        mItemHeight = (int) (1.5 * (double) mItemWidth);
    }

    @Override
    public OverviewGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_overview_griditem, parent, false);
        parent.getWidth();
        return new OverviewGridAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OverviewGridAdapter.ViewHolder viewHolder, int position) {
        final MediaProvider.Video item = getItem(position);

        int padding = PixelUtils.getPixelsFromDp(viewHolder.itemView.getContext(), 2);
        int double_padding = padding * 2;
        int top_padding = (position == 0 || position == 1) ? padding * 2 : padding;
        if(position % 2 == 0) {
            viewHolder.itemView.setPadding(double_padding, top_padding, padding, padding);
        } else {
            viewHolder.itemView.setPadding(padding, top_padding, double_padding, padding);
        }

        Picasso.with(viewHolder.coverImage.getContext()).load(item.image)
                .placeholder(R.drawable.transparant)
                .resize(mItemWidth, mItemHeight)
                .into(viewHolder.coverImage);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public MediaProvider.Video getItem(int position) {
        return mItems.get(position);
    }

    public void setOnItemClickListener(OverviewGridAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }
}

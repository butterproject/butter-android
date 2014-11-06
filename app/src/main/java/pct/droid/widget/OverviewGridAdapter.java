package pct.droid.widget;

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


public class OverviewGridAdapter extends RecyclerView.Adapter<OverviewGridAdapter.ViewHolder> {

    ArrayList<MediaProvider.Video> mItems;
    OverviewGridAdapter.OnItemClickListener mItemClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.coverImage)
        ImageView coverImage;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
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

    public OverviewGridAdapter(ArrayList<MediaProvider.Video> items) {
        mItems = items;
    }

    @Override
    public OverviewGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_overview_griditem, parent, false);
        return new OverviewGridAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(OverviewGridAdapter.ViewHolder viewHolder, int position) {
        final MediaProvider.Video item = getItem(position);

        Picasso.with(viewHolder.coverImage.getContext()).load(item.image)
                .placeholder(R.drawable.popcorn_logo)
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

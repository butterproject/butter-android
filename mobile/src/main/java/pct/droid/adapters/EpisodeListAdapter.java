package pct.droid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.base.providers.media.models.Show;

public class EpisodeListAdapter extends BaseAdapter {

    private List<Show.Episode> mData;
    private LayoutInflater mInflater;
    private int mColor = 0x0;

    class ViewHolder {
        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }

        @InjectView(R.id.number)
        TextView number;
        @InjectView(R.id.title)
        TextView title;
    }

    public EpisodeListAdapter(LayoutInflater inflater, List<Show.Episode> data, int color) {
        mData = data;
        mInflater = inflater;
        mColor = color;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Show.Episode getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.episode_list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Show.Episode episode = getItem(position);
        holder.title.setText(episode.title);
        holder.number.setText("Ep. " + episode.episode);
        holder.number.setTextColor(mColor);

        return convertView;
    }

}

package pct.droid.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;

public class SubtitleAdapter extends BaseAdapter {

    private ArrayList<String> mData;
    private LayoutInflater mInflater;

    class ViewHolder {
        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }
        @InjectView(android.R.id.text1)
        TextView text1;
    }

    public SubtitleAdapter(Context context, ArrayList<String> data) {
        mData = data;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            holder = new ViewHolder(convertView);
            holder.text1.setPadding(32, 0, 0, 0);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String lang = getItem(position);
        if(!lang.equals("no-subs")) {
            Locale locale;
            if(lang.contains("-")) {
                locale = new Locale(lang.substring(0, 2), lang.substring(3, 5));
            } else {
                locale = new Locale(lang);
            }
            holder.text1.setText(locale.getDisplayName());
        } else {
            holder.text1.setText(R.string.disable_subs);
        }

        return convertView;
    }
}

package pct.droid.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.Locale;

import pct.droid.R;

public class SubtitleAdapter extends StringArrayAdapter {

    public SubtitleAdapter(Context context, String[] data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);
        ViewHolder holder = (ViewHolder) convertView.getTag();

        String language = getItem(position);
        if (!language.equals("no-subs")) {
            Locale locale;
            if (language.contains("-")) {
                locale = new Locale(language.substring(0, 2), language.substring(3, 5));
            } else {
                locale = new Locale(language);
            }
            holder.text1.setText(locale.getDisplayName(locale));
        } else {
            holder.text1.setText(R.string.disable_subs);
        }

        return convertView;
    }

}

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

package butter.droid.ui.media.detail.show.season.list;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import butter.droid.R;
import butter.droid.provider.base.Episode;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;
import java.util.Locale;

public class EpisodeListAdapter extends BaseAdapter {

    private final LayoutInflater inflater;

    @ColorInt private int color;
    private List<Episode> data;

    public EpisodeListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (data == null) {
            return 0;
        } else {
            return data.size();
        }
    }

    @Override
    public Episode getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.episode_list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Episode episode = getItem(position);
        holder.bind(episode);

        return convertView;
    }

    public void setData(@ColorInt int color, List<Episode> data) {
        this.color = color;
        this.data = data;
    }

    class ViewHolder {

        @BindView(R.id.info) TextView number;
        @BindView(R.id.title) TextView title;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void bind(Episode episode) {

            if (!TextUtils.isEmpty(episode.getTitle())) {
                title.setText(episode.getTitle());
            } else {
                title.setText(R.string.no_title_available);
            }

            number.setText(String.format(Locale.US, "E%d", episode.getEpisode()));
            number.setTextColor(color);

        }

    }

}

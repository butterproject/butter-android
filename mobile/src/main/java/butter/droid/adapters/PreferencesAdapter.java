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

package butter.droid.adapters;

import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import butter.droid.R;
import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.PrefItem;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PreferencesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String[] keys;
    private Map<String, PrefItem> items;

    final int NORMAL = 0, HEADER = 1;

    public PreferencesAdapter() {
    }

    public void setItems(String[] keys, Map<String, PrefItem> items) {
        this.keys = keys;
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case HEADER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_subheader, parent, false);
                return new PreferencesAdapter.HeaderHolder(v);
            case NORMAL:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_icon_twoline_item, parent, false);
                return new PreferencesAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == NORMAL) {
            ViewHolder itemViewHolder = (ViewHolder) viewHolder;
            PrefItem item = items.get(keys[position]);
            itemViewHolder.itemView.setClickable(item.isClickable());
            itemViewHolder.icon.setImageResource(item.getIconResource());
            itemViewHolder.icon.setColorFilter(ButterApplication.getAppContext().getResources().getColor(R.color.text_color), PorterDuff.Mode.SRC_IN);
            itemViewHolder.text1.setText(item.getTitleRes());
            itemViewHolder.text2.setText(item.getSubtitle());

            if (item.getValue() instanceof Boolean) {
                itemViewHolder.checkBox.setVisibility(View.VISIBLE);
                itemViewHolder.checkBox.setChecked((boolean) item.getValue());
            } else {
                itemViewHolder.checkBox.setVisibility(View.GONE);
            }
        } else if (getItemViewType(position) == HEADER) {
            HeaderHolder headerViewHolder = (HeaderHolder) viewHolder;
            headerViewHolder.itemView.setText(items.get(keys[position]).getTitleRes());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(keys[position]).isTitle()) {
            return HEADER;
        } else {
            return NORMAL;
        }
    }

    public PrefItem getItem(int position) {
        return items.get(keys[position]);
    }

    public void updateItem(int position, PrefItem item) {
        items.put(keys[position], item);
        notifyItemChanged(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.icon) ImageView icon;
        @BindView(android.R.id.text1) TextView text1;
        @BindView(android.R.id.text2) TextView text2;
        @BindView(android.R.id.checkbox) CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public class HeaderHolder extends RecyclerView.ViewHolder {

        TextView itemView;

        public HeaderHolder(View itemView) {
            super(itemView);
            this.itemView = (TextView) itemView;
        }

    }

}

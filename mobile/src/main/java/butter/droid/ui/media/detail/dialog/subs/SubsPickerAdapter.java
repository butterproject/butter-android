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

package butter.droid.ui.media.detail.dialog.subs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butter.droid.R;
import butter.droid.ui.media.detail.dialog.subs.SubsPickerAdapter.SubViewHolder;
import butter.droid.ui.media.detail.model.UiSubItem;

public class SubsPickerAdapter extends Adapter<SubViewHolder> {

    private final LayoutInflater inflater;

    @Nullable private List<UiSubItem> items;

    public SubsPickerAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull @Override public SubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubViewHolder(inflater.inflate(R.layout.item_single_choice, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull SubViewHolder holder, int position) {
        UiSubItem item = getItem(position);
        ImageView checkView = holder.itemView.findViewById(android.R.id.icon);
        checkView.setVisibility(item.isSelected() ? View.VISIBLE : View.INVISIBLE);

        TextView nameView = holder.itemView.findViewById(android.R.id.text1);
        String name = item.getName();
        if (name == null) {
            nameView.setText(R.string.no_subs);
        } else {
            nameView.setText(name);
        }
    }

    public void setItems(List<UiSubItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull public UiSubItem getItem(int position) {
        return items.get(position);
    }

    @Override public int getItemCount() {
        List<UiSubItem> items = this.items;
        if (items == null) {
            return 0;
        } else {
            return items.size();
        }
    }

    static class SubViewHolder extends ViewHolder {

        SubViewHolder(View itemView) {
            super(itemView);
        }
    }

}

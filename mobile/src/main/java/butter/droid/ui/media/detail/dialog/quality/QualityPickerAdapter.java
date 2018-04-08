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

package butter.droid.ui.media.detail.dialog.quality;

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
import butter.droid.ui.media.detail.dialog.quality.QualityPickerAdapter.QualityViewHolder;
import butter.droid.ui.media.detail.dialog.quality.model.UiQuality;

public class QualityPickerAdapter extends Adapter<QualityViewHolder> {

    private final LayoutInflater inflater;

    @Nullable private List<UiQuality> items;

    public QualityPickerAdapter(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull @Override public QualityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QualityViewHolder(inflater.inflate(R.layout.item_single_choice, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull QualityViewHolder holder, int position) {
        UiQuality item = getItem(position);
        ImageView checkView = holder.itemView.findViewById(android.R.id.icon);
        checkView.setVisibility(item.isSelected() ? View.VISIBLE : View.INVISIBLE);

        TextView nameView = holder.itemView.findViewById(android.R.id.text1);
        nameView.setText(item.getName());
    }

    public void setItems(List<UiQuality> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull public UiQuality getItem(int position) {
        return items.get(position);
    }

    @Override public int getItemCount() {
        List<UiQuality> items = this.items;
        if (items == null) {
            return 0;
        } else {
            return items.size();
        }
    }

    static class QualityViewHolder extends ViewHolder {

        QualityViewHolder(View itemView) {
            super(itemView);
        }
    }

}

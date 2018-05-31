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

package butter.droid.ui.main.genre.list;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butter.droid.R;
import butter.droid.ui.main.genre.list.GenreAdapter.ViewHolder;
import butter.droid.ui.main.genre.list.model.UiGenre;
import butterknife.BindView;
import butterknife.ButterKnife;
import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<ViewHolder> {

    private final LayoutInflater layoutInflater;
    private List<UiGenre> items;

    private int selectedColor;
    private int normalColor;

    public GenreAdapter(Context context) {
        this.layoutInflater = LayoutInflater.from(context);

        selectedColor = ContextCompat.getColor(context, R.color.selectable_focused);
        normalColor = ContextCompat.getColor(context, android.R.color.transparent);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.list_singleline_item, parent, false));
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        UiGenre item = getItem(position);

        holder.itemView.setBackgroundColor(item.isSelected() ? selectedColor : normalColor);
        holder.text1.setText(item.getLabel());
    }

    public void setItems(List<UiGenre> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (items != null) {
            return items.size();
        } else {
            return 0;
        }
    }

    public UiGenre getItem(int position) {
        return items.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.text1) TextView text1;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}

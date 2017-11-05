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

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butter.droid.R;
import butter.droid.ui.media.detail.dialog.subs.SubsPickerDialog.SubsPickerCallback;
import butter.droid.ui.media.detail.model.UiSubItem;

public class SubsPickerAdapter extends StaticAdapter<UiSubItem, View> {

    @NonNull private final SubsPickerCallback callback;

    public SubsPickerAdapter(final ViewGroup containerViewGroup, @NonNull final SubsPickerCallback callback) {
        super(containerViewGroup);
        this.callback = callback;
    }

    @Override protected View createView(final LayoutInflater inflater, final ViewGroup parent) {
        return inflater.inflate(R.layout.item_single_choice, parent, false);

    }

    @Override protected void bindView(final View view, final UiSubItem item, final int position) {
        ImageView checkView = view.findViewById(android.R.id.icon);
        checkView.setVisibility(item.isSelected() ? View.VISIBLE : View.INVISIBLE);

        TextView nameView = view.findViewById(android.R.id.text1);
        String name = item.getName();
        if (name == null) {
            nameView.setText(R.string.no_subs);
        } else {
            nameView.setText(name);
        }

        view.setOnClickListener(v -> callback.onSubsItemSelected(position, item));
    }
    
}

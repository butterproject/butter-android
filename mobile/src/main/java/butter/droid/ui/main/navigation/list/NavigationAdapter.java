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

package butter.droid.ui.main.navigation.list;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import butter.droid.R;
import butter.droid.ui.main.navigation.NavigationDrawerFragment;
import butter.droid.ui.main.navigation.NavigationDrawerFragment.AbsNavDrawerItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;

    private List<NavigationDrawerFragment.AbsNavDrawerItem> items;
    private final int normalColor;
    private final int checkedColor;
    private final int checkedBackgroundRes;
    private final int normalBackgroundRes;

    public NavigationAdapter(@NonNull Context context) {
        normalColor = ContextCompat.getColor(context, R.color.nav_drawer_deselected);
        checkedColor = ContextCompat.getColor(context, R.color.primary);
        normalBackgroundRes = R.drawable.selectable_nav_background;
        checkedBackgroundRes = R.color.nav_drawer_selected_bg;
    }

    public void setItems(List<NavigationDrawerFragment.AbsNavDrawerItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_drawer_header, parent, false);
                return new HeaderHolder(view);
            case VIEW_TYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_drawer_list_item, parent, false);
                return new ItemRowHolder(view);
            default:
                throw new IllegalStateException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case VIEW_TYPE_HEADER:
                onBindHeaderViewHolder((HeaderHolder) holder);
                break;
            case VIEW_TYPE_ITEM:
                onBindItemViewHolder((ItemRowHolder) holder, position);
                break;
            default:
                throw new IllegalStateException("Unknown view type");
        }

    }

    private void onBindHeaderViewHolder(HeaderHolder holder) {
        //do nothing for now
        holder.backgroundImageView.setBackgroundResource(R.color.primary_dark);
        holder.profileImageView.setVisibility(View.VISIBLE);
        holder.profileImageView.setImageResource(R.drawable.butter_profile);
    }

    private void onBindItemViewHolder(ItemRowHolder viewHolder, int position) {
        AbsNavDrawerItem item = getItem(position);

//        if(item.isSwitch()) {
//            if(item.showProgress()) {
//                viewHolder.checkbox.setVisibility(View.INVISIBLE);
//                viewHolder.progressBar.setVisibility(View.VISIBLE);
//            } else {
//                viewHolder.checkbox.setVisibility(View.VISIBLE);
//                viewHolder.progressBar.setVisibility(View.INVISIBLE);
//            }
//            viewHolder.checkbox.setChecked(item.getSwitchValue());
//        } else {
//            viewHolder.checkbox.setVisibility(View.INVISIBLE);
//            viewHolder.progressBar.setVisibility(View.INVISIBLE);
//        }

        viewHolder.title.setText(item.getTitle());

        boolean isSelected = item.isSelected();
        viewHolder.title.setTextColor(isSelected ? checkedColor : normalColor);
        viewHolder.itemView.setBackgroundResource(isSelected ? checkedBackgroundRes : normalBackgroundRes);
//        vh.itemView.setBackgroundResource(isSelected ? R.color.nav_drawer_highlight : 0);

        if (item.getIcon() > 0) {
            viewHolder.icon.setImageResource(item.getIcon());

            if (isSelected) {
                viewHolder.icon.setColorFilter(checkedColor, PorterDuff.Mode.SRC_IN);
            } else {
                viewHolder.icon.setColorFilter(normalColor, PorterDuff.Mode.SRC_IN);
            }
        }


    }

    @Override
    public int getItemCount() {
        if (items != null) {
            return items.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).getType() == AbsNavDrawerItem.TYPE_HEADER) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    public NavigationDrawerFragment.AbsNavDrawerItem getItem(int position) {
        if (position < 0 || items.size() <= position) {
            return null;
        } else {
            return items.get(position);
        }
    }

    /**
     * Accounts for non Navigation Item rows and returns a position minus the correct offset
     *
     * @param position
     * @return
     */
    public int getCorrectPosition(int position) {
        return position - 1;
    }

    class ItemRowHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.icon) ImageView icon;
        @BindView(android.R.id.text1) TextView title;
        @BindView(android.R.id.checkbox) Switch checkbox;
        @BindView(android.R.id.progress) ProgressBar progressBar;

        public ItemRowHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public Switch getSwitch() {
            return checkbox;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.bg_imageview) ImageView backgroundImageView;
        @BindView(R.id.profile_imageview) CircleImageView profileImageView;
        @BindView(R.id.title_textview) TextView titleTextView;
        @BindView(R.id.subtitle_textview) TextView subtitleTextView;

        public HeaderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}

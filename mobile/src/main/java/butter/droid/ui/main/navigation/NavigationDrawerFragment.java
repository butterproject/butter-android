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

package butter.droid.ui.main.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import javax.inject.Inject;

import butter.droid.R;
import butter.droid.adapters.decorators.OneShotDividerDecorator;
import butter.droid.base.manager.provider.ProviderManager.ProviderType;
import butter.droid.base.widget.recycler.RecyclerClickListener;
import butter.droid.base.widget.recycler.RecyclerItemClickListener;
import butter.droid.ui.main.MainActivity;
import butter.droid.ui.main.navigation.list.NavigationAdapter;

public class NavigationDrawerFragment extends Fragment implements NavigationDrawerView, RecyclerClickListener {

    /** Remember the position of the selected item. */
    static final String STATE_SELECTED_POSITION
            = "butter.droid.ui.main.navigation.NavigationDrawerFragment.selectedItemPosition";


    @Inject NavigationDrawerPresenter presenter;

    RecyclerView recyclerView;

    private NavigationAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity) getActivity()).getComponent()
                .navigtionDrawerBuilder()
                .navigationDrawerModule(new NavigationDrawerModule(this))
                .build()
                .inject(this);

        int savedPosition = 0;
        if (savedInstanceState != null) {
            savedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

        presenter.onCreate(savedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        return recyclerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new NavigationAdapter(getActivity());

        recyclerView.addItemDecoration(new OneShotDividerDecorator(getActivity(), adapter.getItemCount() - 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), this));

        presenter.onViewCreated();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        presenter.onSavedInstanceState(outState);
    }

    @Override public void onItemClick(View view, int position) {
        presenter.onMenuItemClicked(position, adapter.getItem(position));
    }

    @Override public void populateItems(List<AbsNavDrawerItem> navigationItems) {
        adapter.setItems(navigationItems);
    }

    @Override public void itemsUpdated() {
        adapter.notifyDataSetChanged();
    }

    /**
     * Called when a list item is selected.
     * <p/>
     * Updates the state of the list, closes the drawer, and fowards the event to the parent activity to handle.
     */
    public void selectProvider(@ProviderType int providerType) {
        presenter.selectProvider(providerType);
    }


    public abstract static class AbsNavDrawerItem {

        // region IntDef

        @IntDef({TYPE_HEADER, TYPE_PROVIDER, TYPE_SCREEN})
        @Retention(RetentionPolicy.SOURCE)
        public @interface NavType {
        }

        public static final int TYPE_HEADER = 0;
        public static final int TYPE_PROVIDER = 1;
        public static final int TYPE_SCREEN = 2;

        // endregion IntDef

        @StringRes private final int title;
        @DrawableRes private final int icon;
        private boolean selected;

        public AbsNavDrawerItem(@StringRes int title, @DrawableRes int icon) {
            this.title = title;
            this.icon = icon;
        }

        public int getTitle() {
            return title;
        }

        public int getIcon() {
            return icon;
        }

        @NavType public abstract int getType();

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    public static class ScreenNavDrawerItem extends AbsNavDrawerItem {

        @NonNull private final Class<? extends Activity> activityClass;

        public ScreenNavDrawerItem(@StringRes int title, @DrawableRes int icon,
                @NonNull Class<? extends Activity> activityClass) {
            super(title, icon);
            this.activityClass = activityClass;
        }

        @Override public int getType() {
            return TYPE_SCREEN;
        }

        @NonNull public Class<? extends Activity> getActivityClass() {
            return activityClass;
        }
    }

    static class HeaderNavDrawerItem extends AbsNavDrawerItem {

        public HeaderNavDrawerItem() {
            super(0, 0);
        }

        @Override public int getType() {
            return TYPE_HEADER;
        }
    }

    public static class ProviderNavDrawerItem extends AbsNavDrawerItem {

        @ProviderType private final int providerType;

        public ProviderNavDrawerItem(@StringRes int title, @DrawableRes int icon, @ProviderType int providerType) {
            super(title, icon);
            this.providerType = providerType;
        }

        @ProviderType public int getProviderType() {
            return providerType;
        }

        @Override public int getType() {
            return TYPE_PROVIDER;
        }

    }

}

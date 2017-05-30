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

package butter.droid.tv.ui.preferences.chooser;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.View;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.tv.R;
import java.util.List;

public class TVPreferencesListFragment extends GuidedStepFragment {

    private static final String TITLE_ARG = "butter.droid.tv.ui.preferences.chooser.TVPreferencesListFragment.titleres";
    private static final String ITEMS_ARG = "butter.droid.tv.ui.preferences.chooser.TVPreferencesListFragment.items";
    private static final String CURRENT_POS = "butter.droid.tv.ui.preferences.chooser.TVPreferencesListFragment.current";

    private SelectionListener listener;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSelectedActionPosition(getArguments().getInt(CURRENT_POS, 0));
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String title = getString(getArguments().getInt(TITLE_ARG)).toUpperCase(LocaleUtils.getCurrent());
        return new GuidanceStylist.Guidance(title, null, getString(R.string.app_name), null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        String[] items = getArguments().getStringArray(ITEMS_ARG);
        int current = getArguments().getInt(CURRENT_POS, -1);
        int index = 0;
        if (items != null) {
            for (String item : items) {
                actions.add(
                        new GuidedAction.Builder(getActivity())
                                .id(index)
                                .checked(index == current)
                                .title(item)
                                .build()
                );
                index++;
            }
        }
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        super.onGuidedActionClicked(action);
        listener.onSelect((int) action.getId());
        getFragmentManager().popBackStack();
    }

    public static TVPreferencesListFragment newInstance(@StringRes int title, @NonNull String[] items, int currentPos,
            @NonNull SelectionListener listener) {
        Bundle args = new Bundle();
        args.putInt(TITLE_ARG, title);
        args.putStringArray(ITEMS_ARG, items);
        args.putInt(CURRENT_POS, currentPos);

        TVPreferencesListFragment fragment = new TVPreferencesListFragment();
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    public interface SelectionListener {

        void onSelect(int position);
    }
}

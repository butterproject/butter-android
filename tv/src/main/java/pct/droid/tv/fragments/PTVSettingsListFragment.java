package pct.droid.tv.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.View;

import java.util.List;

import pct.droid.tv.R;

public class PTVSettingsListFragment extends GuidedStepFragment {

    private static final String TITLE_ARG = "titleres", ITEMS_ARG = "items", CURRENT_POS = "current";

    private SelectionListener mListener;


    public static PTVSettingsListFragment newInstance(@NonNull String title, @NonNull String[] items, int currentPos, @NonNull SelectionListener listener) {
        Bundle args = new Bundle();
        args.putString(TITLE_ARG, title);
        args.putStringArray(ITEMS_ARG, items);
        args.putInt(CURRENT_POS, currentPos);

        PTVSettingsListFragment fragment = new PTVSettingsListFragment();
        fragment.setArguments(args);
        fragment.mListener = listener;
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSelectedActionPosition(getArguments().getInt(CURRENT_POS, 0));
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getArguments().getString(TITLE_ARG, ""), null, getString(R.string.app_name), null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        String[] items = getArguments().getStringArray(ITEMS_ARG);
        int current = getArguments().getInt(CURRENT_POS, -1);
        int i = 0;
        if (items != null) {
            for(String item : items) {
                actions.add(
                    new GuidedAction.Builder()
                            .id(i)
                            .checked(i == current)
                            .title(item)
                            .build()
                );
                i++;
            }
        }

        super.onCreateActions(actions, savedInstanceState);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        super.onGuidedActionClicked(action);
        mListener.onSelect((int) action.getId());
        getFragmentManager().popBackStack();
    }

    interface SelectionListener {
        void onSelect(int position);
    }
}

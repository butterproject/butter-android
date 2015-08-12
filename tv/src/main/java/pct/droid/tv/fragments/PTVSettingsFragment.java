package pct.droid.tv.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.View;

import java.util.List;

import pct.droid.base.preferences.PrefItem;
import pct.droid.base.preferences.PreferencesHandler;
import pct.droid.tv.R;

public class PTVSettingsFragment extends GuidedStepFragment implements PreferencesHandler {

    private List<GuidedAction> mActions;
    private List<PrefItem> mPrefs;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setSelectedActionPosition(0);
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.preferences), null, getString(R.string.app_name), null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {

        int index = 0;
        mPrefs = PreferencesHandler.ItemsGenerator.generate(getActivity(), this, true);

        for(PrefItem item : mPrefs) {
            actions.add(generateAction(index, item));
            index++;
        }

        mActions = actions;

        super.onCreateActions(actions, savedInstanceState);
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        super.onGuidedActionClicked(action);
        int currentPos = getSelectedActionPosition();
        mPrefs.get(currentPos).onClick();
        setSelectedActionPosition(currentPos);
    }

    private GuidedAction generateAction(long id, PrefItem item) {
        GuidedAction.Builder builder = new GuidedAction.Builder()
                .id(id)
                .hasNext(item.hasNext())
                .enabled(item.isClickable())
                .infoOnly(item.isTitle() || !item.isClickable())
                .title(item.getTitle());

        if (!item.isTitle()) {
            builder.description(item.getSubtitle());
            if(item.getDefaultValue() instanceof Boolean) {
                builder.checked((Boolean) item.getValue());
            }
        }

        return builder.build();
    }

    @Override
    public void openListSelection(String title, String[] items, SelectionMode mode, Object currentValue, int lowLimit, int highLimit, final OnSelectionListener onClickListener) {
        if(mode == SelectionMode.SIMPLE_CHOICE) {
            int currentPosition = (int) currentValue;
            if(currentPosition == items.length - 1) {
                currentPosition = 0;
            } else {
                currentPosition++;
            }
            onClickListener.onSelection(currentPosition, null);
            updateAction(getSelectedActionPosition());
        } else if(mode == SelectionMode.ADVANCED_CHOICE) {
            PTVSettingsListFragment fragment = PTVSettingsListFragment.newInstance(title, items, (int) currentValue, new PTVSettingsListFragment.SelectionListener() {
                @Override
                public void onSelect(int position) {
                    onClickListener.onSelection(position, null);
                    updateAction(getSelectedActionPosition());
                }
            });
            GuidedStepFragment.add(getFragmentManager(), fragment);
        }
        // TODO: Other modes
    }

    @Override
    public void showMessage(String message) {
        if(message.equals(PreferencesHandler.ABOUT)) {
            // todo: show about activity or something
        } else {
            // todo: show self-hiding message (snackbar?)
        }
    }

    private void updateAction(int position) {
        GuidedAction action = mActions.get(position);
        action.setLabel2(mPrefs.get(position).getSubtitle());
        mActions.set(position, action);
    }
}

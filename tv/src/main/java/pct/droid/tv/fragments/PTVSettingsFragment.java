package pct.droid.tv.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import pct.droid.base.content.preferences.PrefItem;
import pct.droid.base.content.preferences.PreferencesHandler;
import pct.droid.base.utils.LocaleUtils;
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
                .title(item.isTitle() ? item.getTitle().toUpperCase(LocaleUtils.getCurrent()) : item.getTitle());

        if (!item.isTitle()) {
            builder.description(item.getSubtitle());
            if(item.getDefaultValue() instanceof Boolean) {
                builder.checked((Boolean) item.getValue());
            }
        }

        return builder.build();
    }

    @Override
    public void openListSelection(String title, String[] items, SelectionMode mode, Object currentValue, final int lowLimit, int highLimit, final OnSelectionListener onClickListener) {
        PTVSettingsListFragment fragment;

        switch (mode) {
            case SIMPLE_CHOICE:
            case ADVANCED_CHOICE:
                fragment = PTVSettingsListFragment.newInstance(title, items, (int) currentValue, new PTVSettingsListFragment.SelectionListener() {
                    @Override
                    public void onSelect(int position) {
                        onClickListener.onSelection(position, null);
                        updateAction(getSelectedActionPosition());
                    }
                });
                GuidedStepFragment.add(getFragmentManager(), fragment);
                break;
            case NUMBER:
                final String[] array = new String[highLimit - lowLimit + 1];
                for(int i = 0; i <= highLimit - lowLimit; i++) {
                    array[i] = Integer.toString(i + lowLimit);
                }

                fragment = PTVSettingsListFragment.newInstance(title, array, ((int) currentValue - lowLimit), new PTVSettingsListFragment.SelectionListener() {
                    @Override
                    public void onSelect(int position) {
                        onClickListener.onSelection(0, position + lowLimit);
                        updateAction(getSelectedActionPosition());
                    }
                });
                GuidedStepFragment.add(getFragmentManager(), fragment);
                break;
            case COLOR:
                String[] colors = getResources().getStringArray(R.array.subtitle_colors);
                final Integer[] colorCodes = new Integer[] {
                        Color.WHITE, Color.BLACK, Color.YELLOW, Color.RED, Color.BLUE, Color.MAGENTA, Color.GREEN, Color.DKGRAY, Color.LTGRAY
                };
                fragment = PTVSettingsListFragment.newInstance(title, colors, Arrays.asList(colorCodes).indexOf(currentValue), new PTVSettingsListFragment.SelectionListener() {
                    @Override
                    public void onSelect(int position) {
                        onClickListener.onSelection(position, colorCodes[position]);
                        updateAction(getSelectedActionPosition());
                    }
                });
                GuidedStepFragment.add(getFragmentManager(), fragment);
                break;

            case DIRECTORY:
            default:
                // Nothing
                break;
        }
    }

    @Override
    public void showMessage(String message) {
        if(message.equals(PreferencesHandler.ABOUT)) {
            // todo: show about activity or something
        } else {
            Toast.makeText(getActivity(), R.string.restart_effect, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAction(int position) {
        GuidedAction action = mActions.get(position);
        action.setLabel2(mPrefs.get(position).getSubtitle());
        mActions.set(position, action);

        super.onCreateActions(mActions, null);
    }
}

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

package butter.droid.tv.ui.preferences;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.View;
import android.widget.Toast;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.content.preferences.Prefs.PrefKey;
import butter.droid.base.manager.internal.updater.ButterUpdateManager;
import butter.droid.tv.R;
import butter.droid.tv.TVButterApplication;
import butter.droid.tv.ui.update.TVUpdateActivity;
import butter.droid.tv.ui.preferences.chooser.TVPreferencesListFragment;
import butter.droid.tv.ui.preferences.chooser.TVPreferencesListFragment.SelectionListener;
import butter.droid.tv.ui.about.TvAboutFragment;
import butter.droid.tv.ui.preferences.fragment.TVChangeLogDialogFragment;
import butter.droid.tv.ui.preferences.fragment.TVWebViewFragment;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

public class TVPreferencesFragment extends GuidedStepFragment implements TVPreferencesView {

    @Inject TVPreferencesPresenter presenter;
    @Inject ButterUpdateManager butterUpdateManager;
    @Inject PreferencesHandler preferencesHandler;

    @Override public void onCreate(Bundle savedInstanceState) {
        TVButterApplication.getAppContext()
                .getComponent()
                .preferencesComponentBuilder()
                .preferencesModule(new TVPreferencesModule(this))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState);

        presenter.onCreate();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setSelectedActionPosition(0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        butterUpdateManager.setListener(new ButterUpdateManager.Listener() {
            @Override
            public void updateAvailable(String filePath) {
                startActivity(TVUpdateActivity.newIntent(getActivity()));
            }
        });
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(getString(R.string.preferences), null, getString(R.string.app_name), null);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        presenter.createActions(actions);
    }

    @Override public void onDestroy() {
        presenter.onDestroy();

        super.onDestroy();
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        super.onGuidedActionClicked(action);
        int currentPos = getSelectedActionPosition();
        presenter.itemSelected(currentPos);
    }

    @Override
    public void openSimpleChoiceSelector(@PrefKey final String key, @StringRes int title, String[] items,
            int currentValue) {
        TVPreferencesListFragment fragment = TVPreferencesListFragment.newInstance(title, items, currentValue,
                new SelectionListener() {
                    @Override public void onSelect(int position) {
                        presenter.onSimpleChaiseItemSelected(key, position);
                    }
                });
        GuidedStepFragment.add(getFragmentManager(), fragment);
    }

    @Override public void openColorSelector(@PrefKey final String key, @StringRes int title, int value) {
        final String[] colors = getResources().getStringArray(R.array.subtitle_colors);
        final Integer[] colorCodes = new Integer[]{
                Color.WHITE, Color.BLACK, Color.YELLOW, Color.RED, Color.BLUE, Color.MAGENTA, Color.GREEN, Color.DKGRAY,
                Color.LTGRAY
        };
        TVPreferencesListFragment fragment = TVPreferencesListFragment.newInstance(title, colors,
                Arrays.asList(colorCodes).indexOf(value),
                new TVPreferencesListFragment.SelectionListener() {
                    @Override
                    public void onSelect(int position) {
                        presenter.onColorSelected(key, colorCodes[position]);
                    }
                });
        GuidedStepFragment.add(getFragmentManager(), fragment);
    }

    @Override
    public void openNumberSelector(@PrefKey final String key, @StringRes int title, int value, final int min, int max) {
        final String[] array = new String[max - min + 1];
        for (int i = 0; i <= max - min; i++) {
            array[i] = Integer.toString(i + min);
        }

        TVPreferencesListFragment fragment = TVPreferencesListFragment.newInstance(title, array, (value - min),
                new TVPreferencesListFragment.SelectionListener() {
                    @Override
                    public void onSelect(int position) {
                        presenter.onNumberSelected(key, position + min);
                    }
                });
        GuidedStepFragment.add(getFragmentManager(), fragment);
    }

    @Override public void openDirectorySelector(@PrefKey String key, @StringRes int title, String value) {
        throw new UnsupportedOperationException("Directory picking is not supported on TV devices.");
    }

    @Override public void openPreciseNumberSelector(@PrefKey final String key, @StringRes int title, int value, final int min,
            int max) {
        final String[] array = new String[max - min + 1];
        for (int i = 0; i <= max - min; i++) {
            array[i] = Integer.toString(i + min);
        }

        TVPreferencesListFragment fragment = TVPreferencesListFragment.newInstance(title, array, (value - min),
                new TVPreferencesListFragment.SelectionListener() {
                    @Override
                    public void onSelect(int position) {
                        presenter.onNumberSelected(key, position + min);
                    }
                });
        GuidedStepFragment.add(getFragmentManager(), fragment);
    }

    @Override
    public void openPreciseSmallNumberSelector(@PrefKey final String key, @StringRes int title, int value, final int min,
            int max) {
        final String[] array = new String[max - min + 1];
        for (int i = 0; i <= max - min; i++) {
            array[i] = Integer.toString(i + min);
        }

        TVPreferencesListFragment fragment = TVPreferencesListFragment.newInstance(title, array, (value - min),
                new TVPreferencesListFragment.SelectionListener() {
                    @Override
                    public void onSelect(int position) {
                        presenter.onNumberSelected(key, position + min);
                    }
                });
        GuidedStepFragment.add(getFragmentManager(), fragment);
    }

    @Override public void openBrowser(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            final Uri uri = intent.getData();
            final TVWebViewFragment fragment = TVWebViewFragment.newInstance(uri);
            final FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(android.R.id.content, fragment, TVWebViewFragment.TAG).addToBackStack(TVWebViewFragment.TAG).commit();
        } else {
            startActivity(intent);
        }
    }

    @Override public void openChangelog() {
        new TVChangeLogDialogFragment().show(getFragmentManager(), TVChangeLogDialogFragment.TAG);
    }

    @Override public void showMessage(@StringRes int message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override public void updateAction(int position, GuidedAction action) {
        GuidedAction actionById = findActionById(action.getId());
        actionById.setEnabled(action.isEnabled());
        actionById.setDescription(action.getDescription());
        actionById.setChecked(action.isChecked());

        notifyActionChanged(findActionPositionById(action.getId()));
    }

    @Override public void showAboutScreen() {
        GuidedStepFragment.add(getFragmentManager(), new TvAboutFragment());
    }

}

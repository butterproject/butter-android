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

package butter.droid.ui.preferences;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import butter.droid.MobileButterApplication;
import butter.droid.R;
import butter.droid.activities.AboutActivity;
import butter.droid.activities.base.ButterBaseActivity;
import butter.droid.adapters.PreferencesAdapter;
import butter.droid.base.content.preferences.PrefItem;
import butter.droid.base.content.preferences.Prefs.PrefKey;
import butter.droid.base.fragments.dialog.ChangeLogDialogFragment;
import butter.droid.base.fragments.dialog.NumberPickerDialogFragment;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.manager.updater.ButterUpdateManager;
import butter.droid.base.utils.ResourceUtils;
import butter.droid.base.widget.recycler.RecyclerClickListener;
import butter.droid.base.widget.recycler.RecyclerItemClickListener;
import butter.droid.fragments.dialog.ColorPickerDialogFragment;
import butter.droid.fragments.dialog.NumberDialogFragment;
import butter.droid.fragments.dialog.SeekBarDialogFragment;
import butter.droid.utils.ToolbarUtils;
import butterknife.BindView;

public class PreferencesActivity extends ButterBaseActivity implements PreferencesView, RecyclerClickListener {

    private static final String FRAGMENT_DIALOG_PICKER = "fragment_dialog_picker";

    @Inject PreferencesPresenter presenter;
    @Inject ButterUpdateManager updateManager;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.rootLayout) ViewGroup rootLayout;

    private PreferencesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MobileButterApplication.getAppContext()
                .getComponent()
                .preferencesComponentBuilder()
                .preferencesModule(new PreferencesModule(this))
                .build()
                .inject(this);

        super.onCreate(savedInstanceState, R.layout.activity_preferences);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.preferences);

        ToolbarUtils.updateToolbarHeight(this, toolbar);

        adapter = new PreferencesAdapter();
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        recyclerView.setAdapter(adapter);

        presenter.onCreate();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        presenter.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        toolbar.setMinimumHeight(
                (int) ResourceUtils.getAttributeDimension(this, this.getTheme(), R.attr.actionBarSize));
    }

    @Override public void onItemClick(View view, int position) {
        PrefItem item = adapter.getItem(position);
        presenter.itemSelected(item);
    }

    @Override public void displayItems(String[] keys, Map<String, PrefItem> items) {
        adapter.setItems(keys, items);
    }

    @Override
    public void openSimpleChoiceSelector(@PrefKey final String key, @StringRes int title, String[] items, int value) {
        openListDialog(title, items, value, new OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                presenter.onSimpleChaiseItemSelected(key, which);
                dialog.dismiss();
            }
        });
    }

    @Override public void openColorSelector(@PrefKey final String key, @StringRes int title, int value) {
        Bundle args = new Bundle();
        args.putString(ColorPickerDialogFragment.TITLE, getString(title));
        args.putInt(ColorPickerDialogFragment.DEFAULT_VALUE, value);

        ColorPickerDialogFragment dialogFragment = new ColorPickerDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setOnResultListener(new ColorPickerDialogFragment.ResultListener() {
            @Override
            public void onNewValue(int value) {
                presenter.onColorSelected(key, value);
            }
        });
        dialogFragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    @Override
    public void openNumberSelector(@PrefKey final String key, @StringRes int title, int value, int min, int max) {
        Bundle args = new Bundle();
        args.putString(SeekBarDialogFragment.TITLE, getString(title));
        args.putInt(SeekBarDialogFragment.MAX_VALUE, max);
        args.putInt(SeekBarDialogFragment.MIN_VALUE, min);
        args.putInt(SeekBarDialogFragment.DEFAULT_VALUE, value);

        SeekBarDialogFragment dialogFragment = new SeekBarDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setOnResultListener(new SeekBarDialogFragment.ResultListener() {
            @Override
            public void onNewValue(int value) {
                presenter.onNumberSelected(key, value);
            }
        });
        dialogFragment.show(getFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    @Override public void openDirectorySelector(@PrefKey final String key, @StringRes int title, String value) {
        String[] directoryOptions = {getString(R.string.storage_automatic), getString(R.string.storage_choose)};

        openListDialog(title, directoryOptions, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                if (position == 0) {
                    presenter.clearPreference(key);
                } else {
                    DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                            .newDirectoryName(getString(R.string.app_name))
                            .build();

                    final DirectoryChooserFragment directoryChooserFragment = DirectoryChooserFragment.newInstance(
                            config);
                    directoryChooserFragment.setDirectoryChooserListener(
                            new DirectoryChooserFragment.OnFragmentInteractionListener() {
                                @Override
                                public void onSelectDirectory(String s) {
                                    File f = new File(s);
                                    if (f.canWrite()) {
//                                                onSelectionListener.onSelection(0, s);
                                        directoryChooserFragment.dismiss();
                                    } else {
                                        Toast.makeText(PreferencesActivity.this, R.string.not_writable,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelChooser() {
                                    directoryChooserFragment.dismiss();
                                }
                            });

                    dialog.dismiss();
                    directoryChooserFragment.show(getFragmentManager(), FRAGMENT_DIALOG_PICKER);
                }
            }
        });
    }

    @Override public void openPreciseNumberSelector(@PrefKey final String key, @StringRes int title, int value, int min,
            int max) {
        Bundle args = new Bundle();
        args.putString(NumberDialogFragment.TITLE, getString(title));
        args.putInt(NumberDialogFragment.MAX_VALUE, max);
        args.putInt(NumberDialogFragment.MIN_VALUE, min);
        args.putInt(NumberDialogFragment.DEFAULT_VALUE, value);

        NumberDialogFragment dialogFragment = new NumberDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setOnResultListener(new NumberDialogFragment.ResultListener() {
            @Override
            public void onNewValue(int value) {
                presenter.onNumberSelected(key, value);
            }
        });
        dialogFragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    @Override
    public void openPreciseSmallNumberSelector(@PrefKey final String key, @StringRes int title, int value, int min,
            int max) {
        Bundle args = new Bundle();
        args.putString(NumberPickerDialogFragment.TITLE, getString(title));
        args.putInt(NumberPickerDialogFragment.MAX_VALUE, max);
        args.putInt(NumberPickerDialogFragment.MIN_VALUE, min);
        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, value);

        NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setOnResultListener(new NumberPickerDialogFragment.ResultListener() {
            @Override
            public void onNewValue(int value) {
                presenter.onNumberSelected(key, value);
            }
        });
        dialogFragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    @Override public void openActivity(Intent intent) {
        startActivity(intent);
    }

    @Override public void openChangelog() {
        new ChangeLogDialogFragment().show(getSupportFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    @Override public void updateItem(int position, PrefItem preferenceItem) {
        adapter.updateItem(position, preferenceItem);
    }

    @Override public void showMessage(@StringRes int message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override public void showAboutScreen() {
        openActivity(AboutActivity.getIntent(this));
    }

    private void openListDialog(@StringRes int title, String[] items, int currentItem,
            OnClickListener listener) {
        Bundle args = new Bundle();
        args.putString(StringArraySelectorDialogFragment.TITLE, getString(title));
        args.putStringArray(StringArraySelectorDialogFragment.ARRAY, items);
        args.putInt(StringArraySelectorDialogFragment.MODE, StringArraySelectorDialogFragment.NORMAL);
        args.putInt(StringArraySelectorDialogFragment.POSITION, currentItem);

        StringArraySelectorDialogFragment dialogFragment = new StringArraySelectorDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setDialogClickListener(listener);
        dialogFragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG_PICKER);

    }

    public static Intent getIntent(Context context) {
        return new Intent(context, PreferencesActivity.class);
    }

}

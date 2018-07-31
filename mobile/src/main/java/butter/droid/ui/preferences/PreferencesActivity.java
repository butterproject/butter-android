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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butter.droid.R;
import butter.droid.base.content.preferences.PrefItem;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.content.preferences.Prefs.PrefKey;
import butter.droid.base.fragments.dialog.NumberPickerDialogFragment;
import butter.droid.base.fragments.dialog.StringArraySelectorDialogFragment;
import butter.droid.base.utils.ResourceUtils;
import butter.droid.base.widget.recycler.RecyclerClickListener;
import butter.droid.base.widget.recycler.RecyclerItemClickListener;
import butter.droid.ui.ButterBaseActivity;
import butter.droid.ui.about.AboutActivity;
import butter.droid.ui.preferences.dialog.ColorPickerDialogFragment;
import butter.droid.ui.preferences.dialog.NumberDialogFragment;
import butter.droid.ui.preferences.dialog.SeekBarDialogFragment;
import butter.droid.ui.preferences.fragment.ChangeLogDialogFragment;
import butter.droid.utils.ButterCustomTabActivityHelper;
import butter.droid.utils.ToolbarUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PreferencesActivity extends ButterBaseActivity implements PreferencesView, RecyclerClickListener {

    private static final String FRAGMENT_DIALOG_PICKER = "fragment_dialog_picker";
    private static final int OPEN_DIRECTORY_SELECTOR_PERMISSION = 1;

    @Inject PreferencesPresenter presenter;
    @Inject PreferencesHandler preferencesHandler;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.root_layout) ViewGroup rootLayout;

    private PreferencesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        ButterKnife.bind(this);

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
        final PrefItem item = adapter.getItem(position);
        presenter.itemSelected(item);
    }

    @Override public void displayItems(String[] keys, Map<String, PrefItem> items) {
        adapter.setItems(keys, items);
    }

    @Override
    public void openSimpleChoiceSelector(@PrefKey final String key, @StringRes int title, String[] items, int value) {
        openListDialog(title, items, value, (dialog, which) -> {
            presenter.onSimpleChaiseItemSelected(key, which);
            dialog.dismiss();
        });
    }

    @Override public void openColorSelector(@PrefKey final String key, @StringRes int title, @ColorInt int value) {
        ColorPickerDialogFragment fragment = ColorPickerDialogFragment.newInstance(getString(title), value,
                value1 -> presenter.onColorSelected(key, value1));
        fragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    @Override
    public void openNumberSelector(@PrefKey final String key, @StringRes int title, int value, int min, int max) {
        SeekBarDialogFragment dialogFragment = SeekBarDialogFragment.newInstance(getString(title), max, min, value);
        dialogFragment.setOnResultListener(value1 -> presenter.onNumberSelected(key, value1));
        dialogFragment.show(getFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    @Override public void openDirectorySelector(@PrefKey final String key, @StringRes int title, String value) {
        boolean isStoragePermissionGranted =
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED;

        if (!isStoragePermissionGranted) {
            ActivityCompat.requestPermissions(this, new String[] {WRITE_EXTERNAL_STORAGE},  OPEN_DIRECTORY_SELECTOR_PERMISSION);
            return;
        }

        final String[] directoryOptions = {getString(R.string.storage_automatic), getString(R.string.storage_choose)};

        openListDialog(title, directoryOptions, -1, (dialog, position) -> {
            if (position == 0) {
                presenter.clearPreference(key);
            } else {
                final DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.DIR_SELECT;
                properties.root = new File(DialogConfigs.DEFAULT_DIR);
                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.offset = new File(DialogConfigs.DEFAULT_DIR);

                final FilePickerDialog filePickerDialog = new FilePickerDialog(PreferencesActivity.this,
                        properties);
                filePickerDialog.setDialogSelectionListener(files -> {
                    final String path = files[0];
                    presenter.onFolderSelected(key, path);
                });
                filePickerDialog.show();
            }
        });
    }

    @Override public void openPreciseNumberSelector(@PrefKey final String key, @StringRes int title, int value, int min,
            int max) {
        NumberDialogFragment fragment = NumberDialogFragment
                .newInstance(getString(title), max, min, value, value1 -> presenter.onNumberSelected(key, value1));
        fragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    @Override
    public void openPreciseSmallNumberSelector(@PrefKey final String key, @StringRes int title, int value, int min,
            int max) {
        final Bundle args = new Bundle();
        args.putString(NumberPickerDialogFragment.TITLE, getString(title));
        args.putInt(NumberPickerDialogFragment.MAX_VALUE, max);
        args.putInt(NumberPickerDialogFragment.MIN_VALUE, min);
        args.putInt(NumberPickerDialogFragment.DEFAULT_VALUE, value);

        final NumberPickerDialogFragment dialogFragment = new NumberPickerDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setOnResultListener(value1 -> presenter.onNumberSelected(key, value1));
        dialogFragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    @Override public void openBrowser(Intent intent) {
        final Uri url = intent.getData();
        ButterCustomTabActivityHelper.openCustomTab(this, url);
    }

    @Override
    public void openChangelog() {
        new ChangeLogDialogFragment().show(getSupportFragmentManager(), ChangeLogDialogFragment.TAG);
    }

    @Override public void updateItem(int position, PrefItem preferenceItem) {
        adapter.updateItem(position, preferenceItem);
    }

    @Override public void showMessage(@StringRes int message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override public void showAboutScreen() {
        startActivity(AboutActivity.getIntent(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == OPEN_DIRECTORY_SELECTOR_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openDirectorySelector(Prefs.STORAGE_LOCATION, R.string.storage_location, preferencesHandler.getStorageLocation());
            }
        }

    }

    private void openListDialog(@StringRes int title, String[] items, int currentItem, OnClickListener listener) {
        final Bundle args = new Bundle();
        args.putString(StringArraySelectorDialogFragment.TITLE, getString(title));
        args.putStringArray(StringArraySelectorDialogFragment.ARRAY, items);
        args.putInt(StringArraySelectorDialogFragment.MODE, StringArraySelectorDialogFragment.NORMAL);
        args.putInt(StringArraySelectorDialogFragment.POSITION, currentItem);

        final StringArraySelectorDialogFragment dialogFragment = new StringArraySelectorDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.setDialogClickListener(listener);
        dialogFragment.show(getSupportFragmentManager(), FRAGMENT_DIALOG_PICKER);
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, PreferencesActivity.class);
    }

}

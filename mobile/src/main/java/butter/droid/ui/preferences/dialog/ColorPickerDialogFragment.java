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

package butter.droid.ui.preferences.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

import butter.droid.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ColorPickerDialogFragment extends DialogFragment {

    private static final String TITLE = "butter.droid.ui.preferences.dialog.ColorPickerDialogFragment.title";
    private static final String DEFAULT_VALUE = "butter.droid.ui.preferences.dialog.ColorPickerDialogFragment.default_val";

    private ResultListener onResultListener;

    @BindView(R.id.picker) ColorPicker colorPicker;
    @BindView(R.id.svbar) SVBar svBar;
    @BindView(R.id.opacitybar) OpacityBar opacityBar;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_dialog_colorpicker, null);
        ButterKnife.bind(this, view);

        colorPicker.addSVBar(svBar);
        colorPicker.addOpacityBar(opacityBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (getArguments() == null || !getArguments().containsKey(TITLE) || onResultListener == null) {
            return builder.create();
        }

        if (getArguments().containsKey(DEFAULT_VALUE)) {
            int color = getArguments().getInt(DEFAULT_VALUE);
            colorPicker.setColor(color);
            colorPicker.setOldCenterColor(color);
            colorPicker.setNewCenterColor(color);
        }

        builder.setView(view)
                .setTitle(getArguments().getString(TITLE))
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    onResultListener.onNewValue(colorPicker.getColor());
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        return builder.create();
    }

    public void setOnResultListener(ResultListener resultListener) {
        onResultListener = resultListener;
    }

    public static ColorPickerDialogFragment newInstance(String title, @ColorInt int value, ResultListener listener) {
        Bundle args = new Bundle();
        args.putString(ColorPickerDialogFragment.TITLE, title);
        args.putInt(ColorPickerDialogFragment.DEFAULT_VALUE, value);

        ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
        fragment.setArguments(args);
        fragment.setOnResultListener(listener);
        return fragment;
    }

    public interface ResultListener {
        void onNewValue(int value);
    }

}

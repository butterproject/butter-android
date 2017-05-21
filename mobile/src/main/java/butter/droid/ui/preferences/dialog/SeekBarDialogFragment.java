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
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import butter.droid.R;

public class SeekBarDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "butter.droid.ui.preferences.dialog.SeekBarDialogFragment.title";
    private static final String ARG_MAX_VALUE = "butter.droid.ui.preferences.dialog.SeekBarDialogFragment.max_val";
    private static final String ARG_MIN_VALUE = "butter.droid.ui.preferences.dialog.SeekBarDialogFragment.min_val";
    private static final String ARG_DEFAULT_VALUE = "butter.droid.ui.preferences.dialog.SeekBarDialogFragment.default_val";

    private ResultListener onResultListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle arguments = getArguments();
        if (arguments == null || !arguments.containsKey(ARG_MAX_VALUE) || !arguments.containsKey(ARG_MIN_VALUE)
                || !arguments.containsKey(ARG_TITLE) || onResultListener == null) {
            return builder.create();
        }

        final int defaultValue = arguments.getInt(ARG_DEFAULT_VALUE, arguments.getInt(ARG_MAX_VALUE) / 2);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        final SeekBar seekbar = new SeekBar(getActivity());
        seekbar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        seekbar.setMax(arguments.getInt(ARG_MAX_VALUE));
        seekbar.setProgress(defaultValue);

        final TextView textSpeed = new TextView(getActivity());
        textSpeed.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textSpeed.setGravity(Gravity.CENTER);
        textSpeed.setTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Medium);
        textSpeed.setText(defaultValue + " Kb/s");

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int position, boolean fromUser) {
                textSpeed.setText(position + " Kb/s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        layout.addView(seekbar);
        layout.addView(textSpeed);

        builder
                .setView(layout)
                .setTitle(arguments.getString(ARG_TITLE))
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onResultListener.onNewValue(seekbar.getProgress() * 1000);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

        return builder.create();
    }

    public void setOnResultListener(ResultListener resultListener) {
        onResultListener = resultListener;
    }

    public static SeekBarDialogFragment newInstance(String title, int max, int min, int defaultValue) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_MAX_VALUE, max);
        args.putInt(ARG_MIN_VALUE, min);
        args.putInt(ARG_DEFAULT_VALUE, defaultValue);

        SeekBarDialogFragment fragment = new SeekBarDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface ResultListener {

        void onNewValue(int value);
    }

}

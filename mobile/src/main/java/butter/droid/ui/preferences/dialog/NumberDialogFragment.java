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
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import butter.droid.R;

public class NumberDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "butter.droid.ui.preferences.dialog.NumberDialogFragment.title";
    private static final String ARG_MAX_VALUE = "butter.droid.ui.preferences.dialog.NumberDialogFragment.max_val";
    private static final String ARG_MIN_VALUE = "butter.droid.ui.preferences.dialog.NumberDialogFragment.min_val";
    private static final String ARG_DEFAULT_VALUE = "butter.droid.ui.preferences.dialog.NumberDialogFragment.default_val";

    private ResultListener onResultListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle arguments = getArguments();
        if (arguments == null || !arguments.containsKey(ARG_MAX_VALUE) || !arguments.containsKey(ARG_MIN_VALUE)
                || !arguments.containsKey(ARG_TITLE) || onResultListener == null) {
            return builder.create();
        }

        final int defaultValue = arguments.getInt(ARG_DEFAULT_VALUE, arguments.getInt(ARG_MAX_VALUE) / 2);

        final LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        final int max = arguments.getInt(ARG_MAX_VALUE);
        final int min = arguments.getInt(ARG_MIN_VALUE);

        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint(String.valueOf(defaultValue));
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence charSequence, int in, int i1, Spanned spanned, int i2, int i3) {
                        try {
                            int input = Integer.parseInt(spanned.toString() + charSequence.toString());
                            if (input > max) {
                                return "";
                            } else {
                                return null;
                            }
                        } catch (NumberFormatException nfe) {
                            return "";
                        }
                    }
                }
        });

        layout.addView(editText);

        builder
                .setView(layout)
                .setTitle(arguments.getString(ARG_TITLE))
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int output;
                                if (editText.getText().toString().isEmpty()) {
                                    output = defaultValue;
                                } else {
                                    output = Integer.parseInt(editText.getText().toString());
                                }
                                onResultListener.onNewValue(output < min ? min : output);
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

    public static NumberDialogFragment newInstance(String title, int max, int min, int value, ResultListener listener) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_MAX_VALUE, max);
        args.putInt(ARG_MIN_VALUE, min);
        args.putInt(ARG_DEFAULT_VALUE, value);

        NumberDialogFragment fragment = new NumberDialogFragment();
        fragment.setArguments(args);
        fragment.setOnResultListener(listener);
        return fragment;
    }

    public interface ResultListener {

        void onNewValue(int value);
    }

}

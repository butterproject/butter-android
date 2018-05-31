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

package butter.droid.ui.player.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class OptionDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "butter.droid.ui.player.dialog.OptionDialogFragment.title";
    private static final String ARG_MESSAGE = "butter.droid.ui.player.dialog.OptionDialogFragment.message";
    private static final String ARG_POS_BUT = "butter.droid.ui.player.dialog.OptionDialogFragment.pos_but";
    private static final String ARG_NEG_BUT = "butter.droid.ui.player.dialog.OptionDialogFragment.neg_but";

    private Listener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(ARG_TITLE))
                .setMessage(getArguments().getString(ARG_MESSAGE))
                .setPositiveButton(getArguments().getString(ARG_POS_BUT), (dialog, which) -> {
                    if (listener != null) {
                        listener.onSelectionPositive();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(getArguments().getString(ARG_NEG_BUT), (dialog, which) -> {
                    if (listener != null) {
                        listener.onSelectionNegative();
                    }
                    dialog.dismiss();
                })
                .create();
    }

    public static OptionDialogFragment newInstance(String title, String message, String positiveButton, String negativeButton,
            Listener listener) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_POS_BUT, positiveButton);
        args.putString(ARG_NEG_BUT, negativeButton);

        OptionDialogFragment fragment = new OptionDialogFragment();
        fragment.setListener(listener);
        fragment.setArguments(args);
        return fragment;
    }

    public static void show(FragmentManager fm, String title, String message, String positiveButton, String negativeButton,
            Listener listener) {
        OptionDialogFragment fragment = newInstance(title, message, positiveButton, negativeButton, listener);
        fragment.show(fm, "overlay_fragment");
    }

    public static void show(Context context, FragmentManager fm, int titleRes, int messageRes, int posButtonRes, int negButtonRes,
            Listener listener) {
        show(fm, context.getString(titleRes), context.getString(messageRes), context.getString(posButtonRes),
                context.getString(negButtonRes), listener);
    }

    private void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onSelectionPositive();

        void onSelectionNegative();
    }

}

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

package butter.droid.ui.media.detail.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import butter.droid.R;
import butter.droid.base.ButterApplication;

public class MessageDialogFragment extends DialogFragment {

    private static final String ARG_TITLE = "butter.droid.ui.media.detail.dialog.MessageDialogFragment.title";
    private static final String ARG_MESSAGE = "butter.droid.ui.media.detail.dialog.MessageDialogFragment.message";
    private static final String ARG_CANCELABLE = "butter.droid.ui.media.detail.dialog.MessageDialogFragment.cancelable";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (!arguments.containsKey(ARG_TITLE) || !arguments.containsKey(ARG_MESSAGE)) {
            return super.onCreateDialog(savedInstanceState);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(arguments.getString(ARG_TITLE))
                .setMessage(arguments.getString(ARG_MESSAGE));

        if (arguments.getBoolean(ARG_CANCELABLE, true)) {
            builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            setCancelable(true);
        } else {
            setCancelable(false);
        }

        return builder.create();
    }

    public static MessageDialogFragment newInstance(String title, String message, Boolean cancelable) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_CANCELABLE, cancelable);

        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static void show(FragmentManager fm, String title, String message) {
        show(fm, title, message, true);
    }

    public static void show(FragmentManager fm, String title, String message, Boolean cancelable) {
        MessageDialogFragment fragment = newInstance(title, message, cancelable);
        fragment.show(fm, "overlay_fragment");
    }

    public static void show(FragmentManager fm, int titleRes, int messageRes) {
        show(fm, ButterApplication.getAppContext().getString(titleRes), ButterApplication.getAppContext().getString(messageRes));
    }

    public static void show(FragmentManager fm, int titleRes, int messageRes, Boolean cancelable) {
        show(fm, ButterApplication.getAppContext().getString(titleRes), ButterApplication.getAppContext().getString(messageRes),
                cancelable);
    }
}

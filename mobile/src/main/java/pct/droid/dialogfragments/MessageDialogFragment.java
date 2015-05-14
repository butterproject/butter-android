/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import pct.droid.R;
import pct.droid.base.PopcornApplication;

public class MessageDialogFragment extends DialogFragment {

    public static final String TITLE = "title";
    public static final String MESSAGE = "title";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (!getArguments().containsKey(TITLE) || !getArguments().containsKey(MESSAGE)) {
            return super.onCreateDialog(savedInstanceState);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(TITLE))
                .setMessage(getArguments().getString(MESSAGE))
                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    public static void show(FragmentManager fm, String title, String message) {
        MessageDialogFragment dialogFragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        dialogFragment.setArguments(args);
        dialogFragment.show(fm, "overlay_fragment");
    }

    public static void show(FragmentManager fm, int titleRes, int messageRes) {
        show(fm, PopcornApplication.getAppContext().getString(titleRes), PopcornApplication.getAppContext().getString(messageRes));
    }
}

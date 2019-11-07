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

package butter.droid.tv.ui.preferences.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import butter.droid.base.R;
import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView;

public class TVChangeLogDialogFragment extends DialogFragment {

    public static final String TAG = TVChangeLogDialogFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ChangeLogRecyclerView chgList = (ChangeLogRecyclerView) View.inflate(getActivity(), R.layout.fragment_dialog_changelog, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.changelog)
                .setView(chgList)
                .setPositiveButton(R.string.ok, (dialog, whichButton) -> dialog.dismiss()
                )
                .create();

    }

}

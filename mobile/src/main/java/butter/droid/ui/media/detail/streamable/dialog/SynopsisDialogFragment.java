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

package butter.droid.ui.media.detail.streamable.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.TextView;
import butter.droid.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SynopsisDialogFragment extends DialogFragment {

    private static final String ARG_SYNOPSIS = "butter.droid.ui.media.detail.movie.dialog.SynopsisDialogFragment.synopsis";

    @BindView(R.id.synopsis) TextView synopsisText;

    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_synopsis, null);
        ButterKnife.bind(this, view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setNeutralButton(R.string.close, (dialog, which) -> dialog.dismiss()
                );

        synopsisText.setText(getArguments().getString(ARG_SYNOPSIS));

        return builder.create();
    }

    public static SynopsisDialogFragment newInstance(String synopsis) {
        Bundle args = new Bundle();
        args.putString(ARG_SYNOPSIS, synopsis);

        SynopsisDialogFragment fragment = new SynopsisDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

}

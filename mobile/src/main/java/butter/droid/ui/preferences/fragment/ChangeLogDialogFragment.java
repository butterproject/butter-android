package butter.droid.ui.preferences.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import butter.droid.base.R;
import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView;

public class ChangeLogDialogFragment extends DialogFragment {

    public static final String TAG = ChangeLogDialogFragment.class.getSimpleName();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ChangeLogRecyclerView chgList = (ChangeLogRecyclerView) View.inflate(getActivity(), R.layout.fragment_dialog_changelog, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.changelog)
                .setView(chgList)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();

    }

}

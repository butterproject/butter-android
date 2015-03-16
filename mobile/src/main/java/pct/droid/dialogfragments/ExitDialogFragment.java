package pct.droid.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.FragmentManager;

import pct.droid.base.PopcornApplication;

/**
 * Created by danscott on 14/03/15.
 */
public class ExitDialogFragment extends DialogFragment {

    public static final String TITLE = "title";
    public static final String MESSAGE = "message";
    public static final String POS_BUT = "pos_but";
    public static final String NEG_BUT = "neg_but";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(getArguments().getString(TITLE))
                .setMessage(getArguments().getString(MESSAGE))
                .setPositiveButton(getArguments().getString(POS_BUT), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton(getArguments().getString(NEG_BUT), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    public static void show(FragmentManager fm, String title, String message,
                            String positiveButton, String negativeButton) {
        ExitDialogFragment dialogFragment = new ExitDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        args.putString(POS_BUT, positiveButton);
        args.putString(NEG_BUT, negativeButton);
        dialogFragment.setArguments(args);
        dialogFragment.show(fm, "overlay_fragment");
    }

    public static void show(FragmentManager fm, int titleRes, int messageRes, int posButtonRes, int negButtonRes) {
        show(fm, PopcornApplication.getAppContext().getString(titleRes), PopcornApplication.getAppContext().getString(messageRes),
                PopcornApplication.getAppContext().getString(posButtonRes), PopcornApplication.getAppContext().getString(negButtonRes));
    }

}

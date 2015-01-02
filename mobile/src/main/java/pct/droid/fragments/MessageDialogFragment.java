package pct.droid.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;

import pct.droid.R;
import pct.droid.base.PopcornApplication;

public class MessageDialogFragment extends DialogFragment {

    public static final String TITLE = "title";
    public static final String MESSAGE = "title";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(!getArguments().containsKey(TITLE) || !getArguments().containsKey(MESSAGE)) {
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

    public static void showMessageDialog(FragmentManager fm, String title, String message) {
        MessageDialogFragment dialogFragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        dialogFragment.setArguments(args);
        dialogFragment.show(fm, "overlay_fragment");
    }

    public static void showMessageDialog(FragmentManager fm, int titleRes, int messageRes) {
        showMessageDialog(fm, PopcornApplication.getAppContext().getString(titleRes), PopcornApplication.getAppContext().getString(messageRes));
    }
}

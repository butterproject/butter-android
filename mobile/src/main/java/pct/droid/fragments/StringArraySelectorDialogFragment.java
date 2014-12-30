package pct.droid.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.List;

import pct.droid.R;

public class StringArraySelectorDialogFragment extends DialogFragment {

    public static final String ARRAY = "array", TITLE = "title", MODE = "mode", POSITION = "position";
    public static final int NORMAL = 0, SINGLE_CHOICE = 1;

    private DialogInterface.OnClickListener mOnClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (getArguments() == null || !getArguments().containsKey(ARRAY) || !getArguments().containsKey(TITLE) || mOnClickListener == null) {
            return builder.create();
        }

        Bundle b = getArguments();
        Object array = b.get(ARRAY);
        String[] stringArray;
        if (array instanceof List) {
            stringArray = (String[]) ((List) array).toArray(new String[((List) array).size()]);
        } else if (array instanceof String[]) {
            stringArray = b.getStringArray(ARRAY);
        } else {
            return builder.create();
        }
        String title = b.getString(TITLE);

        if (b.containsKey(MODE) && b.getInt(MODE) == SINGLE_CHOICE) {
            int defaultPosition = -1;
            if (b.containsKey(POSITION)) {
                defaultPosition = b.getInt(POSITION);
            }
            builder.setSingleChoiceItems(stringArray, defaultPosition, mOnClickListener);
        } else {
            builder.setItems(stringArray, mOnClickListener);
        }

        builder
                .setTitle(title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return builder.create();
    }

    public void setDialogClickListener(DialogInterface.OnClickListener dialogClickListener) {
        mOnClickListener = dialogClickListener;
    }

}

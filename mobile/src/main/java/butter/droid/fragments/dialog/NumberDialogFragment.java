package butter.droid.fragments.dialog;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import butter.droid.R;

public class NumberDialogFragment extends DialogFragment {

    public static final String TITLE = "title";
    public static final String MAX_VALUE = "max_val";
    public static final String MIN_VALUE = "min_val";
    public static final String DEFAULT_VALUE = "default_val";

    private ResultListener mOnResultListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (getArguments() == null || !getArguments().containsKey(MAX_VALUE) || !getArguments().containsKey(MIN_VALUE) || !getArguments().containsKey(TITLE) || mOnResultListener == null) {
            return builder.create();
        }


        final int defaultValue =  getArguments().getInt(DEFAULT_VALUE, getArguments().getInt(MAX_VALUE)/2);

        final LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        final int max = getArguments().getInt(MAX_VALUE);
        final int min = getArguments().getInt(MIN_VALUE);

        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint(String.valueOf(defaultValue));
        editText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText.setFilters(new InputFilter[] {new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                try {
                    int input = Integer.parseInt(spanned.toString() + charSequence.toString());
                    if (input > max) {
                        return "";
                    } else {
                        return null;
                    }
                } catch (NumberFormatException nfe) { }
                return "";
            }
        }});


        layout.addView(editText);

        builder
                .setView(layout)
                .setTitle(getArguments().getString(TITLE))
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
                                mOnResultListener.onNewValue(output < min ? min : output);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }
                );

        return builder.create();
    }

    public void setOnResultListener(ResultListener resultListener) {
        mOnResultListener = resultListener;
    }

    public interface ResultListener {
        void onNewValue(int value);
    }

}

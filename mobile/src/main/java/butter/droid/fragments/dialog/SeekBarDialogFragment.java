package butter.droid.fragments.dialog;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import butter.droid.R;

public class SeekBarDialogFragment extends DialogFragment {

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


        int defaultValue =  getArguments().getInt(DEFAULT_VALUE, getArguments().getInt(MAX_VALUE)/2);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        final SeekBar seekbar = new SeekBar(getActivity());
        seekbar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        seekbar.setMax(getArguments().getInt(MAX_VALUE));
        seekbar.setProgress(defaultValue);

        final TextView textSpeed = new TextView(getActivity());
        textSpeed.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textSpeed.setGravity(Gravity.CENTER);
        textSpeed.setTextAppearance(getActivity(), R.style.TextAppearance_AppCompat_Medium);
        textSpeed.setText(defaultValue + " Kb/s");

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textSpeed.setText(i + " Kb/s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        layout.addView(seekbar);
        layout.addView(textSpeed);

        builder
                .setView(layout)
                .setTitle(getArguments().getString(TITLE))
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mOnResultListener.onNewValue(seekbar.getProgress());
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

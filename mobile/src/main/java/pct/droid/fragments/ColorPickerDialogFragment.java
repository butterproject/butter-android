package pct.droid.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;

public class ColorPickerDialogFragment extends DialogFragment {

    public static final String TITLE = "title";
    public static final String DEFAULT_VALUE = "default_val";

    private ResultListener mOnResultListener;

    @InjectView(R.id.picker)
    ColorPicker colorPicker;
    @InjectView(R.id.svbar)
    SVBar svBar;
    @InjectView(R.id.opacitybar)
    OpacityBar opacityBar;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_colorpicker, null, false);
        ButterKnife.inject(this, view);

        colorPicker.addSVBar(svBar);
        colorPicker.addOpacityBar(opacityBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if(getArguments() == null || !getArguments().containsKey(TITLE) || mOnResultListener == null) {
            return builder.create();
        }

        if(getArguments().containsKey(DEFAULT_VALUE)) {
            int color = getArguments().getInt(DEFAULT_VALUE);
            colorPicker.setColor(color);
            colorPicker.setOldCenterColor(color);
            colorPicker.setNewCenterColor(color);
        }

        builder
            .setView(view)
            .setTitle(getArguments().getString(TITLE))
            .setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mOnResultListener.onNewValue(colorPicker.getColor());
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
        public void onNewValue(int value);
    }
}

package pct.droid.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import eu.se_bastiaan.popcorntimeremote.R;

public class SynopsisDialogFragment extends DialogFragment {

    @InjectView(R.id.synopsisText)
    TextView synopsisText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_synopsis, null, false);
        ButterKnife.inject(this, view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
        .setView(view)
        .setNeutralButton(R.string.close,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        if(getArguments().containsKey("text")) {
            synopsisText.setText(getArguments().getString("text"));
        }

        return builder.create();
    }

}

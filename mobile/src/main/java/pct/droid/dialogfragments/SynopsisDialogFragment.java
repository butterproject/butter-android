package pct.droid.dialogfragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;

public class SynopsisDialogFragment extends DialogFragment {

    @InjectView(R.id.synopsis)
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

        if (getArguments().containsKey("text")) {
            synopsisText.setText(getArguments().getString("text"));
        }

        return builder.create();
    }

}

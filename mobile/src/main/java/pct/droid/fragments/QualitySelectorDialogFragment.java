package pct.droid.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.adapters.StringArrayAdapter;

public class QualitySelectorDialogFragment extends DialogFragment {

    public static final String QUALITIES = "qualities";

    @InjectView(R.id.listView)
    ListView listView;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialogselector, null, false);
        ButterKnife.inject(this, view);

        if (getArguments().containsKey(QUALITIES) && getActivity() instanceof Listener) {
            progressBar.setVisibility(View.GONE);

            String[] qualities = getArguments().getStringArray(QUALITIES);
            Arrays.sort(qualities);

            StringArrayAdapter adapter = new StringArrayAdapter(getActivity(), qualities);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    StringArrayAdapter adapter = (StringArrayAdapter) adapterView.getAdapter();
                    ((Listener) getActivity()).onQualitySelected(adapter.getItem(position));
                    dismiss();
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.quality)
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public interface Listener {
        public void onQualitySelected(String quality);
    }

}


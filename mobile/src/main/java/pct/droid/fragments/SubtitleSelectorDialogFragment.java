package pct.droid.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.R;
import pct.droid.adapters.SubtitleAdapter;

public class SubtitleSelectorDialogFragment extends DialogFragment {

    public static final String LANGUAGES = "languages";

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

        if (getArguments().containsKey(LANGUAGES) && getActivity() instanceof Listener) {
            progressBar.setVisibility(View.GONE);

            String[] languages = getArguments().getStringArray(LANGUAGES);
            Arrays.sort(languages);
            String[] adapterLanguages = new String[languages.length + 1];
            adapterLanguages[0] = "no-subs";
            int i = 1;
            for (String language : languages) {
                adapterLanguages[i] = language;
                i++;
            }

            SubtitleAdapter adapter = new SubtitleAdapter(getActivity(), adapterLanguages);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    SubtitleAdapter adapter = (SubtitleAdapter) adapterView.getAdapter();
                    ((Listener) getActivity()).onSubtitleLanguageSelected(adapter.getItem(position));
                    dismiss();
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.subtitles)
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
        public void onSubtitleLanguageSelected(String language);
    }

}


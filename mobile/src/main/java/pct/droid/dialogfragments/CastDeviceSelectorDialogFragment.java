package pct.droid.dialogfragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import pct.droid.R;
import pct.droid.base.casting.CastingDevice;
import pct.droid.base.casting.CastingDeviceAdapter;
import pct.droid.base.casting.CastingManager;

public class CastDeviceSelectorDialogFragment extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CastingDeviceAdapter adapter = new CastingDeviceAdapter(getActivity());
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        CastingDevice device;
                        if(position == 0) {
                            device = null;
                        } else {
                            device = adapter.getItem(position);
                        }
                        CastingManager.getInstance(getActivity()).setDevice(device);
                        dismiss();
                    }
                })
                .setTitle("Select casting device")
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

    public static void show(FragmentManager fm) {
        CastDeviceSelectorDialogFragment fragment = new CastDeviceSelectorDialogFragment();
        fragment.show(fm, "overlay_fragment");
    }

}


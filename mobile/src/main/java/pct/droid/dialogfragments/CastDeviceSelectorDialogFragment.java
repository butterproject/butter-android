/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.dialogfragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.connectsdk.device.ConnectableDevice;

import pct.droid.R;
import pct.droid.base.connectsdk.BeamDeviceAdapter;
import pct.droid.base.connectsdk.BeamManager;

public class CastDeviceSelectorDialogFragment extends DialogFragment {

    private BeamDeviceAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAdapter = new BeamDeviceAdapter(getActivity());
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity())
                .setSingleChoiceItems(mAdapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        ConnectableDevice device;
                        if(position == 0) {
                            device = null;
                        } else {
                            device = mAdapter.getItem(position);
                        }
                        BeamManager.getInstance(getActivity()).setDevice(device);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != mAdapter)
            mAdapter.destroy();
    }

    public static void show(FragmentManager fm) {
        CastDeviceSelectorDialogFragment fragment = new CastDeviceSelectorDialogFragment();
        fragment.show(fm, "overlay_fragment");
    }

}


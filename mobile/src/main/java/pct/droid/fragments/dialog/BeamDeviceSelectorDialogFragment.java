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

package pct.droid.fragments.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import com.connectsdk.device.ConnectableDevice;

import pct.droid.R;
import pct.droid.base.beaming.BeamDeviceAdapter;
import pct.droid.base.beaming.BeamManager;

public class BeamDeviceSelectorDialogFragment extends DialogFragment {

    private BeamDeviceAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        final BeamManager beamManager = BeamManager.getInstance(getActivity());
        if (!beamManager.isConnected()) {
            mAdapter = new BeamDeviceAdapter(getActivity());
            builder = new AlertDialog.Builder(getActivity())
                    .setSingleChoiceItems(mAdapter, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            ConnectableDevice device = mAdapter.getItem(position);
                            BeamManager.getInstance(getActivity()).connect(device);
                            dismiss();
                        }
                    })
                    .setTitle(R.string.select_beaming)
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }
                    );
            return builder.create();
        } else if(beamManager.getConnectedDevice() != null) {
            builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.connected_to) + " " + beamManager.getConnectedDevice().getFriendlyName())
                    .setNeutralButton(R.string.disconnect, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            beamManager.disconnect();
                        }
                    });
            return builder.create();
        }
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mAdapter)
            mAdapter.destroy();
    }

    public static void show(FragmentManager fm) {
        BeamDeviceSelectorDialogFragment fragment = new BeamDeviceSelectorDialogFragment();
        fragment.show(fm, "overlay_fragment");
    }

}


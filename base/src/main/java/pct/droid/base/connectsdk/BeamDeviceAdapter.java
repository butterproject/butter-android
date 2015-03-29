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

package pct.droid.base.connectsdk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.service.AirPlayService;
import com.connectsdk.service.CastService;
import com.connectsdk.service.DLNAService;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.RokuService;
import com.connectsdk.service.command.ServiceCommandError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.base.R;
import pct.droid.base.utils.ThreadUtils;

public class BeamDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private BeamManager mBeamManager;
    private Map<String, ConnectableDevice> mDevices = new HashMap<>();
    private ArrayList<String> mKeys = new ArrayList<>();

    class ViewHolder {
        @InjectView(android.R.id.icon)
        ImageView icon;
        @InjectView(android.R.id.text1)
        TextView text1;
        @InjectView(android.R.id.text2)
        TextView text2;

        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }
    }

    public BeamDeviceAdapter(Context context) {
        mContext = context;
        mBeamManager = BeamManager.getInstance(context);

        mDevices = mBeamManager.getDevices();
        mKeys = new ArrayList<>(mDevices.keySet());

        mBeamManager.addDiscoveryListener(mListener);
    }

    public void destroy() {
        mBeamManager.removeDiscoveryListener(mListener);
    }

    @Override
    public int getCount() {
        return mDevices.size() + 1;
    }

    @Override
    public ConnectableDevice getItem(int position) {
        if(position == 0) {
            return null;
        }
        return mDevices.get(mKeys.get(position - 1));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.casting_dialog_listitem, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(position != 0) {
            ConnectableDevice device = getItem(position);

            for(DeviceService service : device.getServices()) {
                if(service instanceof CastService) {
                    holder.icon.setImageResource(R.drawable.ic_googlecast);
                    holder.text2.setText("Google Cast");
                    break;
                } else if(service instanceof DLNAService) {
                    holder.icon.setImageResource(R.drawable.ic_dlna);
                    holder.text2.setText("DLNA");
                    break;
                } else if(service instanceof AirPlayService) {
                    holder.icon.setImageResource(R.drawable.ic_airplay);
                    holder.text2.setText("AirPlay");
                    break;
                } else if(service instanceof RokuService) {
                    // TODO: add roku icon
                    holder.icon.setImageResource(R.drawable.ic_dlna);
                    holder.text2.setText("Roku");
                    break;
                } else {
                    holder.icon.setImageResource(R.drawable.ic_dlna);
                    holder.text2.setText("Remote");
                    break;
                }
            }

            holder.text1.setText(device.getFriendlyName());
        } else {
            holder.icon.setImageResource(R.drawable.ic_notif_logo);
            holder.text1.setText("Local");
            holder.text2.setText("Popcorn Time");
        }

        return convertView;
    }

    DiscoveryManagerListener mListener = new DiscoveryManagerListener() {
        @Override
        public void onDeviceAdded(DiscoveryManager manager, ConnectableDevice device) {
            mDevices = mBeamManager.getDevices();
            mKeys = new ArrayList<>(mDevices.keySet());
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onDeviceUpdated(DiscoveryManager manager, ConnectableDevice device) {
            mDevices = mBeamManager.getDevices();
            mKeys = new ArrayList<>(mDevices.keySet());
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onDeviceRemoved(DiscoveryManager manager, ConnectableDevice device) {
            mDevices = mBeamManager.getDevices();
            mKeys = new ArrayList<>(mDevices.keySet());
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onDiscoveryFailed(DiscoveryManager manager, ServiceCommandError error) {

        }
    };
}

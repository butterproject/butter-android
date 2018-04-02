/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.manager.internal.beaming;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.discovery.DiscoveryManager;
import com.connectsdk.discovery.DiscoveryManagerListener;
import com.connectsdk.service.AirPlayService;
import com.connectsdk.service.CastService;
import com.connectsdk.service.DLNAService;
import com.connectsdk.service.DeviceService;
import com.connectsdk.service.NetcastTVService;
import com.connectsdk.service.RokuService;
import com.connectsdk.service.WebOSTVService;
import com.connectsdk.service.command.ServiceCommandError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butter.droid.base.R;
import butter.droid.base.utils.ThreadUtils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class BeamDeviceAdapter extends BaseAdapter {

    private final Context context;
    private final BeamManager beamManager;
    private Map<String, ConnectableDevice> devices = new HashMap<>();
    private ArrayList<String> keys = new ArrayList<>();

    class ViewHolder {
        @BindView(android.R.id.icon) AppCompatImageView icon;
        @BindView(android.R.id.text1) TextView text1;
        @BindView(android.R.id.text2) TextView text2;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }

    public BeamDeviceAdapter(Context context, BeamManager beamManager) {
        this.context = context;
        this.beamManager = beamManager;

        devices = beamManager.getDevices();
        keys = new ArrayList<>(devices.keySet());

        beamManager.addDiscoveryListener(mListener);
    }

    public void destroy() {
        beamManager.removeDiscoveryListener(mListener);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public ConnectableDevice getItem(int position) {
        return devices.get(keys.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.casting_dialog_listitem, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ConnectableDevice device = getItem(position);

        int imgResource = R.drawable.ic_dlna;

        Collection<DeviceService> services = device.getServices();
        ArrayList<String> textServices = new ArrayList<>(services.size());

        for (DeviceService service : services) {
            if (service instanceof CastService) {
                imgResource = R.drawable.ic_googlecast;
                textServices.add("Google Cast");
            } else if (service instanceof DLNAService) {
                imgResource = R.drawable.ic_dlna;
                textServices.add("DLNA");
            } else if (service instanceof AirPlayService) {
                imgResource = R.drawable.ic_airplay;
                textServices.add("AirPlay");
            } else if (service instanceof RokuService) {
                imgResource = R.drawable.ic_dlna;
                textServices.add("Roku");
            } else if (service instanceof WebOSTVService) {
                imgResource = R.drawable.ic_dlna;
                textServices.add("webOS TV");
            } else if (service instanceof NetcastTVService) {
                imgResource = R.drawable.ic_dlna;
                textServices.add("Netcast");
            }
        }

        String serviceText;
        if (textServices.size() == 0) {
            serviceText = "Beaming Device";
        } else {
            serviceText = TextUtils.join(",", textServices);
        }

        holder.icon.setImageResource(imgResource);
        holder.text1.setText(device.getFriendlyName());
        holder.text2.setText(serviceText);

        return convertView;
    }

    private DiscoveryManagerListener mListener = new DiscoveryManagerListener() {
        @Override
        public void onDeviceAdded(DiscoveryManager manager, ConnectableDevice device) {
            devices = beamManager.getDevices();
            keys = new ArrayList<>(devices.keySet());
            ThreadUtils.runOnUiThread(() -> notifyDataSetChanged());
        }

        @Override
        public void onDeviceUpdated(DiscoveryManager manager, ConnectableDevice device) {
            devices = beamManager.getDevices();
            keys = new ArrayList<>(devices.keySet());
            ThreadUtils.runOnUiThread(() -> notifyDataSetChanged());
        }

        @Override
        public void onDeviceRemoved(DiscoveryManager manager, ConnectableDevice device) {
            devices = beamManager.getDevices();
            keys = new ArrayList<>(devices.keySet());
            ThreadUtils.runOnUiThread(() -> notifyDataSetChanged());
        }

        @Override
        public void onDiscoveryFailed(DiscoveryManager manager, ServiceCommandError error) {

        }
    };
}

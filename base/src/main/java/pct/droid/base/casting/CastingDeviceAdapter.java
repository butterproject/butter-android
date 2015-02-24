package pct.droid.base.casting;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pct.droid.base.Constants;
import pct.droid.base.R;
import pct.droid.base.casting.airplay.AirPlayDevice;
import pct.droid.base.casting.dlna.DLNADevice;
import pct.droid.base.casting.googlecast.GoogleDevice;
import pct.droid.base.utils.ThreadUtils;

public class CastingDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private CastingManager mCastingManager;
    private CastingDevice[] mDevices = new CastingDevice[0];

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

    public CastingDeviceAdapter(Context context) {
        mContext = context;
        mCastingManager = CastingManager.getInstance(context);

        mDevices = mCastingManager.getDevices();

        mCastingManager.addListener(mListener);
    }

    public void destroy() {
        mCastingManager.removeListener(mListener);
    }

    @Override
    public int getCount() {
        return mDevices.length + 1;
    }

    @Override
    public CastingDevice getItem(int position) {
        if(position == 0) {
            return null;
        }
        return mDevices[position - 1];
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
            CastingDevice device = getItem(position);
            if (device instanceof GoogleDevice) {
                holder.icon.setImageResource(R.drawable.ic_googlecast);
            } else if (device instanceof AirPlayDevice) {
                holder.icon.setImageResource(R.drawable.ic_airplay);
            } else if (device instanceof DLNADevice) {
                holder.icon.setImageResource(R.drawable.ic_dlna);
            }

            holder.text1.setText(device.getName());
            holder.text2.setText(device.getModel());
        } else {
            holder.icon.setImageResource(R.drawable.ic_notif_logo);
            holder.text1.setText("Local");
            holder.text2.setText("Popcorn Time");
        }

        return convertView;
    }

    private CastingListener mListener = new CastingListener() {
        @Override
        public void onConnected(CastingDevice device) {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onCommandFailed(String command, String message) {

        }

        @Override
        public void onConnectionFailed() {

        }

        @Override
        public void onDeviceDetected(CastingDevice device) {
            mDevices = mCastingManager.getDevices();
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onDeviceSelected(CastingDevice device) {

        }

        @Override
        public void onDeviceRemoved(CastingDevice device) {
            mDevices = mCastingManager.getDevices();
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onVolumeChanged(double value, boolean isMute) {

        }

        @Override
        public void onReady() {

        }

        @Override
        public void onPlayBackChanged(boolean isPlaying, float position) {

        }
    };
}

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

public class CastingDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private CastingManager mCastingManager;

    class ViewHolder {
        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
        }

        @InjectView(android.R.id.icon)
        ImageView icon;
        @InjectView(android.R.id.text1)
        TextView text1;
        @InjectView(android.R.id.text2)
        TextView text2;
    }

    public CastingDeviceAdapter(Context context) {
        mContext = context;
        mCastingManager = CastingManager.getInstance(context);

        mCastingManager.getDevices();

        mCastingManager.setListener(new CastingListener() {
            @Override
            public void onConnected(CastingDevice device) {

            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onConnectionFailed() {

            }

            @Override
            public void onDeviceDetected(CastingDevice device) {
                notifyDataSetChanged();
            }

            @Override
            public void onDeviceSelected(CastingDevice device) {

            }

            @Override
            public void onDeviceRemoved(CastingDevice device) {
                notifyDataSetChanged();
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
        });
    }

    @Override
    public int getCount() {
        return mCastingManager.getDevices().length + 1;
    }

    @Override
    public CastingDevice getItem(int position) {
        if(position == 0) {
            return null;
        }
        return mCastingManager.getDevices()[position - 1];
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
            holder.icon.setImageResource(R.drawable.ic_launcher);
            holder.text1.setText("Local");
            try {
                PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                holder.text2.setText("Popcorn Time");
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                holder.text2.setText("");
            }
        }

        return convertView;
    }
}

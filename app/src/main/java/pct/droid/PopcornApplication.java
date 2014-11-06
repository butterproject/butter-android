package pct.droid;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.*;

import org.nodejs.core.NodeJSService;

import java.io.IOException;
import java.util.List;

import pct.droid.utils.LogUtils;

public class PopcornApplication extends Application {

    private Boolean mBound = false;
    private Messenger mService;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent nodeServiceIntent = new Intent(this, NodeJSService.class);
        bindService(nodeServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public Boolean isServiceBound() {
        return mBound;
    }

    public void runScript(String file_name, String args) {
        if (!mBound) return;

        Message msg = Message.obtain(null, NodeJSService.MSG_RUN_SCRIPT, 0, 0);

        Bundle extras = new Bundle();
        extras.putString("file_name", file_name);
        if(args != null) {
            extras.putString("args", args);
        }

        msg.setData(extras);

        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopScript() {
        if (!mBound) return;

        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();

        for(int i = 0; i < runningAppProcesses.size(); i++)
        {
            ActivityManager.RunningAppProcessInfo info = runningAppProcesses.get(i);
            if(info.processName.equalsIgnoreCase("pct.droid:node")){
                android.os.Process.killProcess(info.pid);
            }
        }

        Intent nodeServiceIntent = new Intent(this, NodeJSService.class);
        bindService(nodeServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
            runScript("app", "9000");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };



}

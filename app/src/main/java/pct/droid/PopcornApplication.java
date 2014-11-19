package pct.droid;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.io.File;
import java.util.List;

import pct.droid.services.StreamerService;
import pct.droid.utils.FileUtils;
import pct.droid.utils.LogUtils;
import pct.droid.utils.StorageUtils;

public class PopcornApplication extends Application {

    private Boolean mBound = false;
    private Messenger mService;
    private String mCacheDir;
    private static PopcornApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        Intent nodeServiceIntent = new Intent(this, StreamerService.class);
        bindService(nodeServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

        File path = StorageUtils.getIdealCacheDirectory(this);
        File directory = new File(path, "/torrents/");
        File temp = new File(path, "/torrents/tmp");
        mCacheDir = directory.toString() + "/";
        FileUtils.recursiveDelete(new File(mCacheDir));
        directory.mkdirs();
        temp.mkdirs();

        LogUtils.d("StorageLocations: " + StorageUtils.getAllStorageLocations());
        LogUtils.i("Chosen cache location: " + mCacheDir);
    }

    public Boolean isServiceBound() {
        return mBound;
    }

    public String getStreamDir() {
        return mCacheDir;
    }

    public void startStreamer(String streamUrl) {
        if (!mBound) return;

        LogUtils.i("Start streamer: " + streamUrl);

        Message msg = Message.obtain(null, StreamerService.MSG_RUN_SCRIPT, 0, 0);

        Bundle args = new Bundle();
        args.putString("directory", mCacheDir);
        args.putString("stream_url", streamUrl);
        msg.setData(args);

        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopStreamer() {
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

        File torrentPath = new File(mCacheDir);
        File tmpPath = new File(mCacheDir, "tmp");
        FileUtils.recursiveDelete(torrentPath);
        torrentPath.mkdirs();
        tmpPath.mkdirs();

        startService();
    }

    public void startService() {
        if(mBound) return;
        Intent nodeServiceIntent = new Intent(this, StreamerService.class);
        bindService(nodeServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };

    public static Context getAppContext() {
        return mInstance;
    }


}

package pct.droid.base;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.bugsnag.android.Bugsnag;

import org.videolan.vlc.VLCApplication;

import java.io.File;
import java.util.List;

import pct.droid.base.services.StreamerService;
import pct.droid.base.utils.FileUtils;
import pct.droid.base.utils.LogUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.StorageUtils;

public class PopcornApplication extends VLCApplication {

    private Boolean mBound = false;
    private Messenger mService;
    private String mCacheDir, mShouldBoundUrl = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Bugsnag.register(this, Constants.BUGSNAG_KEY);

        Constants.DEBUG_ENABLED = false;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int flags = packageInfo.applicationInfo.flags;
            Constants.DEBUG_ENABLED = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Intent nodeServiceIntent = new Intent(this, StreamerService.class);
        bindService(nodeServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

        File path = StorageUtils.getIdealCacheDirectory(this);
        File directory = new File(path, "/torrents/");
        mCacheDir = directory.toString() + "/";
        FileUtils.recursiveDelete(new File(mCacheDir));
        FileUtils.recursiveDelete(new File(path + "/subs"));
        directory.mkdirs();

        LogUtils.d("StorageLocations: " + StorageUtils.getAllStorageLocations());
        LogUtils.i("Chosen cache location: " + mCacheDir);

        String versionCode = Integer.toString(BuildConfig.VERSION_CODE);
        if(!PrefUtils.get(this, "versionCode", "0").equals(versionCode)) {
            PrefUtils.save(this, "versionCode", versionCode);
        }
    }

    public Boolean isServiceBound() {
        return mBound;
    }

    public String getStreamDir() {
        return mCacheDir;
    }

    public void startStreamer(String streamUrl) {
        if (!mBound) {
            LogUtils.d("Service not started yet");
            mShouldBoundUrl = streamUrl;
            startService();
            return;
        }

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

            if(mShouldBoundUrl != null && !mShouldBoundUrl.isEmpty()) {
                startStreamer(mShouldBoundUrl);
                mShouldBoundUrl = "";
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };


}

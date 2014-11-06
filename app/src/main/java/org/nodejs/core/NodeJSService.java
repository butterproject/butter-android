package org.nodejs.core;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import pct.droid.PopcornApplication;
import pct.droid.utils.LogUtils;

public class NodeJSService extends Service {

    private static final String TAG = "nodejs-service";
    private static final String NODEJS_PATH = "backend";
    private static final String DEFAULT_PACKAGE = "backend.zip";

    private String mPackageName = DEFAULT_PACKAGE;
    private NodeJSThread mThread = null;
    private Boolean mRunningScript = false;

    final Messenger mMessenger = new Messenger(new IncomingHandler());
    public static final int MSG_RUN_SCRIPT = 0;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                LogUtils.d(TAG, "handleMessage: " + msg.what);
                switch (msg.what) {
                    case MSG_RUN_SCRIPT:
                        Bundle args = msg.getData();
                        String nodeCommand = "";

                        if (args.containsKey("file_name")) {
                            nodeCommand += args.getString("file_name") + ".js";
                        }

                        if (args.containsKey("args")) {
                            nodeCommand += " " + args.getString("args");
                        }

                        if(!nodeCommand.equals("") && nodeCommand.contains(".js")) {
                            runScript(nodeCommand);
                        }
                        break;
                    default:
                        super.handleMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PackageManager pm = getPackageManager();
        ComponentName component = new ComponentName(this, this.getClass());
        ServiceInfo info;

        LogUtils.d(TAG, component.toString());

        try {
            info = pm.getServiceInfo(component, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return START_NOT_STICKY;
        }

        Bundle metaData = info.metaData;

        if(metaData.getString("node_package") != null) {
            mPackageName = metaData.getString("node_package");
        }

        return START_STICKY;
    }

    /** Important Node stuff below **/
    private class NodeJSThread extends Thread {

        private String mFileName;

        public NodeJSThread() {
            mFileName = "app.js";
        }

        public void setFileName(String fileName) {
            mFileName = fileName;
        }

        @Override
        public void run() {
            super.run();

            mRunningScript = true;

            AssetManager assets = NodeJSService.this.getAssets();

            File appPath = NodeJSService.this.getDir(NODEJS_PATH, Context.MODE_PRIVATE);

            File js = new File(appPath, NODEJS_PATH + "/" + "src" + "/" + "app" + "/" + mFileName);
            if (!js.exists()) {
                try {
                    installPackage(assets, mPackageName, appPath);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG, "Error while installing script", e);
                }
            }

            LogUtils.d(TAG, "run :" + js);
            NodeJSCore.run(js.toString());
            LogUtils.d(TAG, "run end");
        }

        @Override
        public void interrupt() {
            super.interrupt();
            LogUtils.d(TAG, "script interrupted");
            mRunningScript = false;
        }
    }

    public static void installPackage(AssetManager assets, String packageName, File targetDir) throws IOException {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        ZipInputStream zin = new ZipInputStream(assets.open(packageName));

        ZipEntry ze = null;

        try {
            while((ze = zin.getNextEntry()) != null) {
                if(ze.isDirectory()) {
                    File path = new File(targetDir, ze.getName());

                    path.mkdirs();
                } else {
                    File path = new File(targetDir, ze.getName());
                    FileOutputStream out = new FileOutputStream(path);

                    LogUtils.d(TAG, "extract " + ze.getName() + " to " + path);

                    byte[] buf = new byte[4096];
                    int len;

                    while((len = zin.read(buf)) != -1) {
                        out.write(buf, 0, len);
                    }

                    out.flush();
                    out.close();
                }
            }
        } finally {
            zin.close();
        }
    }

    public void runScript(String mainJS) throws IOException {
        synchronized(this) {
            if(mThread == null) {
                mThread = new NodeJSThread();
            }

            if (!mThread.isInterrupted() && mRunningScript) {
                mThread.interrupt();
            }
            mThread.setFileName(mainJS);
            mThread.run();
        }
    }

}

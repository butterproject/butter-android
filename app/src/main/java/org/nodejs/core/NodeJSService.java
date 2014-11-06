package org.nodejs.core;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class NodeJSService extends Service {

    private final IBinder mBinder = new ServiceBinder();
    private static final String TAG = "nodejs-service";
    private static final String NODEJS_PATH = "backend";
    private static final String DEFAULT_PACKAGE = "backend.zip";

    private String mPackageName = DEFAULT_PACKAGE;
    private NodeJSTask mTask = null;

    public class ServiceBinder extends Binder {
        public NodeJSService getService() {
            return NodeJSService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PackageManager pm = getPackageManager();
        ComponentName component = new ComponentName(this, this.getClass());
        ServiceInfo info;

        Log.d(TAG, component.toString());

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
    private class NodeJSTask extends AsyncTask<String, Void, String> {

        Context mContext = null;
        boolean running = false;

        NodeJSTask() {
            mContext = NodeJSService.this;
        }

        @Override
        protected String doInBackground(String... params) {
            running = true;

            // Copy files from assets to app path
            AssetManager assets = mContext.getAssets();

            File appPath = mContext.getDir(NODEJS_PATH, Context.MODE_PRIVATE);

            String mainJS = params[0];
            File js = new File(appPath, NODEJS_PATH + "/" + "src" + "/" + "app" + "/" + mainJS);
            if (!js.exists()) {
                try {
                    installPackage(assets, mPackageName, appPath);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG, "Error while installing script", e);
                }
            }
            return js.toString();
        }

        @Override
        protected void onPostExecute(String js) {
            if (js != null) {
                Log.d(TAG, "run :" + js);
                NodeJSCore.run(js);
                Log.d(TAG, "run end");
            } else {
                NodeJSService.this.stopSelf();
            }

            running = false;
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

                    Log.d(TAG, "extract " + ze.getName() + " to " + path);

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
            if(mTask == null) {
                mTask = new NodeJSTask();
            }

            try {
                if (!mTask.running) {
                    mTask.execute(mainJS);
                }
            } catch (IllegalStateException e) {
                mTask.running = true;
            }
        }
    }

}

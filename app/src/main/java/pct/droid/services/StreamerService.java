package pct.droid.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import org.nodejs.core.NodeJSCore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import pct.droid.utils.LogUtils;

public class StreamerService extends Service {

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
                switch (msg.what) {
                    case MSG_RUN_SCRIPT:
                        Bundle args = msg.getData();
                        runScript(args.getString("directory"), args.getString("stream_url"));
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

        LogUtils.d(component.toString());

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

        return START_NOT_STICKY;
    }

    /** Important Node stuff below **/
    private class NodeJSThread extends Thread {

        private String mFileName = "app.js", mDirectory = "", mStreamUrl = "";

        public void setDirectory(String directory) {
            mDirectory = directory;
        }

        public void setStreamUrl(String url) {
            mStreamUrl = url;
        }

        @Override
        public void run() {
            super.run();

            mRunningScript = true;

            AssetManager assets = StreamerService.this.getAssets();

            //File appPath = NodeJSService.this.getDir(NODEJS_PATH, Context.MODE_PRIVATE);
            File appPath = StreamerService.this.getExternalCacheDir();

            File js = new File(appPath, NODEJS_PATH + "/" + mFileName);
            if (!js.exists()) {
                try {
                    installPackage(assets, mPackageName, appPath);
                } catch (IOException e) {
                    LogUtils.e("Error while installing script", e);
                }
            }

            try {
                File status = new File(mDirectory + "/status.json");
                if (status.exists()) {
                    status.delete();
                    status.createNewFile();
                    status.setWritable(true);
                }
            } catch (IOException e) {
                LogUtils.e("Error while creating status.json", e);
            }

            try {
                File status = new File(mDirectory + "/streamer.json");
                if (status.exists()) {
                    status.delete();
                    status.createNewFile();
                    status.setWritable(true);
                }
            } catch (IOException e) {
                LogUtils.e("Error while creating streamer.json", e);
            }

            LogUtils.d("run :" + js);
            File script = new File(appPath, NODEJS_PATH + "/" + "main_node_script.js");
            if(script.exists()) {
                script.delete();
            }

            try {
                script.createNewFile();

                BufferedReader reader = new BufferedReader(new FileReader(js));
                PrintWriter writer = new PrintWriter(new FileWriter(script));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.replace("<stream_url>", mStreamUrl);
                    line = line.replace("<directory>", mDirectory);
                    writer.println(line);
                }

                reader.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            LogUtils.d("populated script");
            NodeJSCore.run(script.toString());
            LogUtils.d("run end");
        }

        @Override
        public void interrupt() {
            super.interrupt();
            LogUtils.d("script interrupted");
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

                    LogUtils.d("extract " + ze.getName() + " to " + path);

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

    public void runScript(String dir, String streamUrl) throws IOException {
        synchronized(this) {
            if(mThread == null) {
                mThread = new NodeJSThread();
            }

            if (!mThread.isInterrupted() && mRunningScript) {
                mThread.interrupt();
            }
            mThread.setDirectory(dir);
            mThread.setStreamUrl(streamUrl);
            mThread.run();
        }
    }

}

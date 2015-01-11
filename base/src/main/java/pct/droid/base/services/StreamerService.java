package pct.droid.base.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.frostwire.jlibtorrent.DHT;
import com.frostwire.jlibtorrent.Downloader;
import com.frostwire.jlibtorrent.Entry;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.LibTorrent;
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.TorrentAlertAdapter;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.Utils;
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import com.frostwire.jlibtorrent.alerts.TorrentErrorAlert;
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

import pct.droid.base.PopcornApplication;
import pct.droid.base.streamer.Status;
import pct.droid.base.utils.FileUtils;
import pct.droid.base.utils.LogUtils;

public class StreamerService extends Service {

    private TorrentThread mThread = null;
    private Boolean mRunningScript = false;
    private Session mSession = new Session();
    private DHT mDht = new DHT(mSession);

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
        return START_NOT_STICKY;
    }

    /**
     * Important Node stuff below *
     */
    private class TorrentThread extends Thread {

        private String mDirectory = "", mStreamUrl = "";

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

            File torrentFile = new File(mDirectory + "/current.torrent");
            torrentFile.delete();

            if(mStreamUrl.startsWith("magnet")) {
                Downloader d = new Downloader(mSession);

                LogUtils.d("Waiting for nodes in DHT");
                mDht.waitNodes(10);
                LogUtils.d("Nodes in DHT: " + mDht.nodes());

                LogUtils.d("Fetching the magnet uri, please wait...");
                byte[] data = d.fetchMagnet(mStreamUrl, 30000);

                if (data != null) {
                    System.out.println(Entry.bdecode(data));

                    try {
                        Utils.writeByteArrayToFile(torrentFile, data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    LogUtils.d("Torrent data saved to: " + torrentFile);
                } else {
                    LogUtils.d("Failed to retrieve the magnet");
                    return;
                }
            } else {
                OkHttpClient client = PopcornApplication.getHttpClient();
                Request request = new Request.Builder().url(mStreamUrl).build();
                try {
                    Response response = client.newCall(request).execute();
                    Utils.writeByteArrayToFile(torrentFile, response.body().bytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            LogUtils.d("Using libtorrent version: " + LibTorrent.version());

            TorrentHandle th = mSession.addTorrent(torrentFile, torrentFile.getParentFile());

            final int[] progress = {0};

            FileStorage fileStorage = th.getTorrentInfo().getFiles();
            long highestFileSize = 0;
            int selectedFile = -1;
            for(int i = 0; i < fileStorage.geNumFiles(); i++) {
                long fileSize = fileStorage.getFileSize(i);
                if(highestFileSize < fileSize) {
                    highestFileSize = fileSize;
                    if(selectedFile > -1) {
                        //th.setFilePriority(selectedFile, Priority.IGNORE);
                    }
                    selectedFile = i;
                    //th.setFilePriority(selectedFile, Priority.SEVEN);
                }
            }

            th.getSwig().set_sequential_download(true);
            th.getSwig().set_share_mode(false);
            th.setUploadLimit(0);

            final String filePath = th.getTorrentInfo().getFileAt(selectedFile).getPath();
            LogUtils.d("Videofile: " + filePath);
            final File statusFile = new File(mDirectory + "/status.json");
            try {
                statusFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mSession.addListener(new TorrentAlertAdapter(th) {
                public void blockFinished(BlockFinishedAlert alert) {
                    TorrentHandle th = alert.getHandle();
                    int p = (int) (th.getStatus().getProgress() * 100);
                    if(p != progress[0]) {
                        progress[0] = p;
                        LogUtils.d("Progress: " + p);
                    }

                    float progress = th.getStatus().getProgress() * 100;
                    int peers = th.getStatus().getNumPeers();
                    int seeds = th.getStatus().getNumSeeds();
                    int speed = th.getStatus().getDownloadPayloadRate();

                    String json = Status.getJSON(progress, speed, peers, seeds, mDirectory + "/" + filePath);
                    try {
                        FileUtils.saveStringFile(json, statusFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void torrentError(TorrentErrorAlert alert) {
                    super.torrentError(alert);
                }

                @Override
                public void addTorrent(AddTorrentAlert alert) {
                    super.addTorrent(alert);
                }

                public void torrentFinished(TorrentFinishedAlert alert) {
                    LogUtils.d("Torrent finished");
                    alert.getHandle().pause();
                }
            });

            th.resume();
            LogUtils.d("Torrent started");
        }

        @Override
        public void interrupt() {
            super.interrupt();
            LogUtils.d("script interrupted");
            mRunningScript = false;
        }
    }

    public void runScript(String dir, String streamUrl) throws IOException {
        synchronized (this) {
            if (mThread == null) {
                mThread = new TorrentThread();
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

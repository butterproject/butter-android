/*
 * This file is part of Popcorn Time.
 *
 * Popcorn Time is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Popcorn Time is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Popcorn Time. If not, see <http://www.gnu.org/licenses/>.
 */

package pct.droid.base.torrent;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.frostwire.jlibtorrent.DHT;
import com.frostwire.jlibtorrent.Downloader;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.SessionSettings;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.TorrentStatus;
import com.frostwire.jlibtorrent.Utils;
import com.frostwire.jlibtorrent.alerts.BlockFinishedAlert;
import com.frostwire.jlibtorrent.alerts.TorrentFinishedAlert;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pct.droid.base.PopcornApplication;
import pct.droid.base.R;
import pct.droid.base.activities.TorrentBaseActivity;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.utils.FileUtils;
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.ThreadUtils;
import timber.log.Timber;

public class TorrentService extends Service {

    private static TorrentService sThis;

    private static final String THREAD_NAME = "TORRENT_SERVICE_THREAD";
    private HandlerThread mThread;
    private Handler mHandler;

    private final Integer mId = 3423423;
    private Session mTorrentSession;
    private DHT mDHT;
    private TorrentHandle mCurrentTorrent;
    private TorrentAlertAdapter mCurrentListener;

    private String mCurrentTorrentUrl = "";
    private File mCurrentVideoLocation;
    private boolean mIsStreaming = false, mIsCanceled = false, mReady = false, mInForeground = false, mIsBound = false;

    private boolean mInitialised = false;

    private IBinder mBinder = new ServiceBinder();
    private List<Listener> mListener = new ArrayList<>();

    private PowerManager.WakeLock mWakeLock;
    private Class mCurrentActivityClass;

    public class ServiceBinder extends Binder {
        public TorrentService getService() {
            return TorrentService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sThis = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();
        mThread.interrupt();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("onBind");
        mIsBound = true;
        initialize();
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        mIsBound = true;
        super.onRebind(intent);
        Timber.d("onRebind");
        initialize();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        Timber.d("onUnbind");

        if(!mIsStreaming) {
            pause();
        } else {
            startForeground();
        }
        mIsBound = false;

        return true;
    }

    public void setCurrentActivity(TorrentBaseActivity activity) {
        mCurrentActivityClass = activity.getClass();
    }

    public void startForeground() {
        Timber.d("startForeground");
        if (mInForeground) return;

        Intent notificationIntent = new Intent(this, mCurrentActivityClass);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(pct.droid.base.R.drawable.ic_notif_logo)
                .setContentTitle("Popcorn Time - " + getString(pct.droid.base.R.string.running))
                .setContentText(getString(R.string.tap_to_resume))
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        startForeground(mId, notification);
        mInForeground = true;
    }

    public void stopForeground() {
        Timber.d("stopForeground");
        mInForeground = false;
        stopForeground(true);
    }

    /**
     * Initialize will setup the thread and handler,
     * and start/resume the torrent session
     */
    private void initialize() {
        if(mInForeground) {
            stopForeground();
        }

        Timber.d("initialize");
        if (mThread != null) {
            mHandler.removeCallbacksAndMessages(null);

            //resume torrent session if needed
            if (mTorrentSession != null && mTorrentSession.isPaused()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Timber.d("Resuming libtorrent session");
                        mTorrentSession.resume();
                    }
                });
            }
            //start DHT if needed
            if (mDHT != null && !mDHT.isRunning()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDHT.start();
                        Timber.d("Nodes in DHT: %s", mDHT.nodes());
                    }
                });

            }
        } else {
            if (mInitialised) return;

            mThread = new HandlerThread(THREAD_NAME);
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Start libtorrent session and init DHT
                    Timber.d("Starting libtorrent session");
                    mTorrentSession = new Session();
                    SessionSettings sessionSettings = mTorrentSession.getSettings();
                    sessionSettings.setAnonymousMode(true);
                    mTorrentSession.setSettings(sessionSettings);
                    Timber.d("Init DHT");
                    mDHT = new DHT(mTorrentSession);
                    mDHT.start();
                    Timber.d("Nodes in DHT: %s", mDHT.nodes());

                    mInitialised = true;
                }
            });
        }
    }

    private void pause() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTorrentSession.pause();
                mDHT.stop();
                Timber.d("Pausing libtorrent session");
            }
        });
    }

    public void streamTorrent(@NonNull final String torrentUrl) {
        Timber.d("streamTorrent");

        //attempt to initialize service
        initialize();

        if (mHandler == null || mIsStreaming) return;

        mIsCanceled = false;
        mReady = false;

        Timber.d("Starting streaming");

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, THREAD_NAME);
        mWakeLock.acquire();

        SessionSettings sessionSettings = mTorrentSession.getSettings();
        sessionSettings.setActiveDHTLimit(PrefUtils.get(this, Prefs.LIBTORRENT_DHT_LIMIT, 200));
        sessionSettings.setConnectionsLimit(PrefUtils.get(this, Prefs.LIBTORRENT_CONNECTION_LIMIT, 200));
        sessionSettings.setDownloadRateLimit(PrefUtils.get(this, Prefs.LIBTORRENT_DOWNLOAD_LIMIT, 0));
        sessionSettings.setUploadRateLimit(PrefUtils.get(this, Prefs.LIBTORRENT_UPLOAD_LIMIT, 0));
        mTorrentSession.setSettings(sessionSettings);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Timber.d("streaming runnable");
                mIsStreaming = true;
                mCurrentTorrentUrl = torrentUrl;

                File saveDirectory = new File(PopcornApplication.getStreamDir());
                saveDirectory.mkdirs();

                File torrentFileDir = new File(saveDirectory, "files");
                torrentFileDir.mkdirs();

                File torrentFile = new File(torrentFileDir, System.currentTimeMillis() + ".torrent");

                if (!torrentFile.exists()) {
                    int fileCreationTries = 0;
                    while (fileCreationTries < 4) {
                        try {
                            fileCreationTries++;
                            if (torrentFileDir.mkdirs() || torrentFileDir.isDirectory()) {
                                Timber.d("Creating torrent file");
                                torrentFile.createNewFile();
                            }
                        } catch (IOException e) {
                            Timber.e(e, "Error on file create");
                        }
                    }

                    if (!getTorrentFile(torrentUrl, torrentFile)) {
                        for (final Listener listener : mListener) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onStreamError(new IOException("Write error"));
                                }
                            });
                        }
                        return;
                    } else if (!torrentFile.exists()) {
                        for (final Listener listener : mListener) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onStreamError(new IOException("Write error"));
                                }
                            });
                        }
                        return;
                    }
                }

                if (!mCurrentTorrentUrl.equals(torrentUrl) || mIsCanceled) {
                    return;
                }


                mCurrentTorrent = mTorrentSession.addTorrent(torrentFile, saveDirectory);
                mCurrentListener = new TorrentAlertAdapter(mCurrentTorrent);
                mTorrentSession.addListener(mCurrentListener);

                TorrentInfo torrentInfo = mCurrentTorrent.getTorrentInfo();
                FileStorage fileStorage = torrentInfo.getFiles();
                long highestFileSize = 0;
                int selectedFile = -1;
                for (int i = 0; i < fileStorage.geNumFiles(); i++) {
                    long fileSize = fileStorage.getFileSize(i);
                    if (highestFileSize < fileSize) {
                        highestFileSize = fileSize;
                        selectedFile = i;
                    }
                }


                mCurrentVideoLocation = new File(saveDirectory, torrentInfo.getFileAt(selectedFile).getPath());

                Timber.d("Video location: %s", mCurrentVideoLocation);

                //post a new runnable which will potentially take along time.
                //this is the runnable which actually starts the streaming.
                //posting this as a runnable will allow other runnables that have been posted
                //execute before this potentially blocking runnable
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        //blocking call.
                        mDHT.waitNodes(30);
                        mCurrentTorrent.setSequentialDownload(true);
                        mCurrentTorrent.resume();
                        for (final Listener listener : mListener) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onStreamStarted();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public void stopStreaming() {
        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();

        stopForeground();

        //remove all callbacks from handler
        mHandler.removeCallbacksAndMessages(null);

        mIsCanceled = true;
        mIsStreaming = false;
        if (mCurrentTorrent != null) {
            mCurrentTorrent.pause();
            mDHT.stop();
            mTorrentSession.removeListener(mCurrentListener);
            mTorrentSession.removeTorrent(mCurrentTorrent);
            mCurrentListener = null;
            mCurrentTorrent = null;
        }

        File saveDirectory = new File(PopcornApplication.getStreamDir());
        File torrentPath = saveDirectory;
        if (!PrefUtils.get(TorrentService.this, Prefs.REMOVE_CACHE, true)) {
            torrentPath = new File(saveDirectory, "files");
        }
        FileUtils.recursiveDelete(torrentPath);
        saveDirectory.mkdirs();

        Timber.d("Stopped torrent and removed files");

        if(!mIsBound) {
            pause();
        }
    }

    public boolean isStreaming() {
        return mIsStreaming;
    }

    public String getCurrentTorrentUrl() {
        return mCurrentTorrentUrl;
    }

    public File getCurrentVideoLocation() {
        return mCurrentVideoLocation;
    }

    public boolean isReady() {
        return mReady;
    }

    public void addListener(@NonNull Listener listener) {
        mListener.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        mListener.remove(listener);
    }

    private boolean getTorrentFile(String torrentUrl, File destination) {
        if (torrentUrl.startsWith("magnet")) {
            Downloader d = new Downloader(mTorrentSession);

            Timber.d("Waiting for nodes in DHT");
            if (mDHT.nodes() < 1) {
                mDHT.start();
                mDHT.waitNodes(1);
            }
            Timber.d("Nodes in DHT: %s", mDHT.nodes());

            Timber.d("Fetching the magnet uri, please wait...");
            byte[] data = d.fetchMagnet(torrentUrl, 30000);

            if (data != null) {
                try {
                    Utils.writeByteArrayToFile(destination, data);
                    Timber.d("Torrent data saved to: %s,", destination);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Timber.d("Failed to retrieve the magnet");
        } else {
            OkHttpClient client = PopcornApplication.getHttpClient();
            Request request = new Request.Builder().url(torrentUrl).build();
            try {
                Response response = client.newCall(request).execute();
                Utils.writeByteArrayToFile(destination, response.body().bytes());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void bindHere(Context context, ServiceConnection serviceConnection) {
        Intent torrentServiceIntent = new Intent(context, TorrentService.class);
        context.bindService(torrentServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void start(Context context) {
        Intent torrentServiceIntent = new Intent(context, TorrentService.class);
        context.startService(torrentServiceIntent);
    }

    public interface Listener {
        public void onStreamStarted();

        public void onStreamError(Exception e);

        public void onStreamReady(File videoLocation);

        public void onStreamProgress(DownloadStatus status);
    }

    protected class TorrentAlertAdapter extends com.frostwire.jlibtorrent.TorrentAlertAdapter {
        private int mLastLoggedProgress = 0;

        public TorrentAlertAdapter(TorrentHandle th) {
            super(th);
        }

        public void blockFinished(BlockFinishedAlert alert) {
            super.blockFinished(alert);
            TorrentHandle th = alert.getHandle();
            if (!th.getInfoHash().equals(mCurrentTorrent.getInfoHash())) return;
            TorrentStatus status = th.getStatus();
            final float progress = status.getProgress() * 100;
            int floorProgress = (int) Math.floor(progress);
            if (floorProgress % 5 == 0 && mLastLoggedProgress != floorProgress) {
                mLastLoggedProgress = (int) Math.floor(progress);
                Timber.d("Torrent progress: %s", progress);
            }
            int bufferProgress = (int) Math.floor(progress * 12);
            if (bufferProgress > 100) bufferProgress = 100;
            final int seeds = status.getNumSeeds();
            final int downloadSpeed = status.getDownloadPayloadRate();

            for (final Listener listener : mListener) {
                final int finalBufferProgress = bufferProgress;
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamProgress(new DownloadStatus(progress, finalBufferProgress, seeds, downloadSpeed));
                    }
                });
            }

            if (bufferProgress == 100 && !mReady) {
                mReady = true;
                Timber.d("onStreamReady");
                for (final Listener listener : mListener) {
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onStreamReady(mCurrentVideoLocation);
                        }
                    });
                }
            }
        }

        @Override
        public void torrentFinished(TorrentFinishedAlert alert) {
            super.torrentFinished(alert);
        }

    }

    protected static void stop() {
        sThis.stopStreaming();
    }

}

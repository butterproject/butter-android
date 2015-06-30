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
import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.Session;
import com.frostwire.jlibtorrent.SessionSettings;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.TorrentAddedAlert;
import com.sjl.foreground.Foreground;
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
import pct.droid.base.utils.PrefUtils;
import pct.droid.base.utils.ThreadUtils;
import timber.log.Timber;

public class TorrentService extends Service {

    private static TorrentService sThis;

    private static final String LIBTORRENT_THREAD_NAME = "TORRENT_SERVICE_THREAD", STREAMING_THREAD_NAME = "TORRENT_STREAMING_THREAD";
    private HandlerThread mLibTorrentThread, mStreamingThread;
    private Handler mLibTorrentHandler, mStreamingHandler;

    private Session mTorrentSession;
    private DHT mDHT;
    private Torrent mCurrentTorrent;

    private String mCurrentTorrentUrl = "";
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
        Foreground.get().addListener(mForegroundListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();
        mLibTorrentThread.interrupt();
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
                .setSmallIcon(R.drawable.ic_notif_logo)
                .setContentTitle("Popcorn Time - " + getString(pct.droid.base.R.string.running))
                .setContentText(getString(R.string.tap_to_resume))
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        Notification notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        startForeground(3423423, notification);
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
        if (mLibTorrentThread != null) {
            mLibTorrentHandler.removeCallbacksAndMessages(null);

            //resume torrent session if needed
            if (mTorrentSession != null && mTorrentSession.isPaused()) {
                mLibTorrentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Timber.d("Resuming libtorrent session");
                        mTorrentSession.resume();
                    }
                });
            }
            //start DHT if needed
            if (mDHT != null && !mDHT.isRunning()) {
                mLibTorrentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDHT.start();
                        Timber.d("Nodes in DHT: %s", mDHT.nodes());
                    }
                });

            }
        } else {
            if (mInitialised) return;

            mLibTorrentThread = new HandlerThread(LIBTORRENT_THREAD_NAME);
            mLibTorrentThread.start();
            mLibTorrentHandler = new Handler(mLibTorrentThread.getLooper());
            mLibTorrentHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Start libtorrent session and init DHT
                    Timber.d("Starting libtorrent session");
                    mTorrentSession = new Session();
                    SessionSettings sessionSettings = mTorrentSession.getSettings();
                    sessionSettings.setAnonymousMode(true);
                    mTorrentSession.setSettings(sessionSettings);
                    mTorrentSession.addListener(mAlertListener);
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
        mLibTorrentHandler.post(new Runnable() {
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

        if (mLibTorrentHandler == null || mIsStreaming) return;

        mIsCanceled = false;
        mReady = false;

        Timber.d("Starting streaming");

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if(mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LIBTORRENT_THREAD_NAME);
        mWakeLock.acquire();

        SessionSettings sessionSettings = mTorrentSession.getSettings();
        sessionSettings.setActiveDHTLimit(PrefUtils.get(this, Prefs.LIBTORRENT_DHT_LIMIT, 200));
        sessionSettings.setConnectionsLimit(PrefUtils.get(this, Prefs.LIBTORRENT_CONNECTION_LIMIT, 200));
        sessionSettings.setDownloadRateLimit(PrefUtils.get(this, Prefs.LIBTORRENT_DOWNLOAD_LIMIT, 0));
        sessionSettings.setUploadRateLimit(PrefUtils.get(this, Prefs.LIBTORRENT_UPLOAD_LIMIT, 0));
        mTorrentSession.setSettings(sessionSettings);

        mStreamingThread = new HandlerThread(STREAMING_THREAD_NAME);
        mStreamingThread.start();
        mStreamingHandler = new Handler(mStreamingThread.getLooper());

        mStreamingHandler.post(new Runnable() {
            @Override
            public void run() {
                Timber.d("streaming runnable");
                mIsStreaming = true;
                mCurrentTorrentUrl = torrentUrl;

                File saveDirectory = new File(PopcornApplication.getStreamDir());
                saveDirectory.mkdirs();

                TorrentInfo torrentInfo = getTorrentInfo(torrentUrl);
                Priority[] priorities = new Priority[torrentInfo.getNumPieces()];
                for (int i = 0; i < priorities.length; i++) {
                    priorities[i] = Priority.NORMAL;
                }

                if (!mCurrentTorrentUrl.equals(torrentUrl) || mIsCanceled) {
                    return;
                }

                mTorrentSession.asyncAddTorrent(torrentInfo, saveDirectory, priorities, null);
            }
        });
    }

    public void stopStreaming() {
        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();

        stopForeground();

        //remove all callbacks from handler
        mLibTorrentHandler.removeCallbacksAndMessages(null);
        mStreamingHandler.removeCallbacksAndMessages(null);

        mIsCanceled = true;
        mIsStreaming = false;
        if (mCurrentTorrent != null) {
            File currentVideoFile = mCurrentTorrent.getVideoFile();

            mCurrentTorrent.pause();
            mTorrentSession.removeListener(mCurrentTorrent);
            mTorrentSession.removeTorrent(mCurrentTorrent.getTorrentHandle());
            mCurrentTorrent = null;

            if (PrefUtils.get(TorrentService.this, Prefs.REMOVE_CACHE, true)) {
                currentVideoFile.delete();
            }
        }

        mStreamingThread.interrupt();

        Timber.d("Stopped torrent and removed files if possible");
    }

    public boolean isStreaming() {
        return mIsStreaming;
    }

    public String getCurrentTorrentUrl() {
        return mCurrentTorrentUrl;
    }

    public File getCurrentVideoLocation() {
        return mCurrentTorrent.getVideoFile();
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

    private TorrentInfo getTorrentInfo(String torrentUrl) {
        if (torrentUrl.startsWith("magnet")) {
            Downloader d = new Downloader(mTorrentSession);

            Timber.d("Waiting for nodes in DHT");
            if(!mDHT.isRunning()) {
                mDHT.start();
            }

            if (mDHT.nodes() < 1) {
                mDHT.waitNodes(30);
            }

            Timber.d("Nodes in DHT: %s", mDHT.nodes());

            Timber.d("Fetching the magnet uri, please wait...");
            byte[] data = d.fetchMagnet(torrentUrl, 30000);

            return TorrentInfo.bdecode(data);
        } else {
            OkHttpClient client = PopcornApplication.getHttpClient();
            Request request = new Request.Builder().url(torrentUrl).build();
            try {
                Response response = client.newCall(request).execute();
                return TorrentInfo.bdecode(response.body().bytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
        void onStreamStarted();

        void onStreamError(Exception e);

        void onStreamReady(File videoLocation);

        void onStreamProgress(DownloadStatus status);
    }

    protected class TorrentListener implements Torrent.Listener {

        @Override
        public void onStreamStarted() {
            for (final Listener listener : mListener) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamStarted();
                    }
                });
            }
        }

        @Override
        public void onStreamError(final Exception e) {
            for (final Listener listener : mListener) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamError(e);
                    }
                });
            }
        }

        @Override
        public void onStreamReady(final File file) {
            for (final Listener listener : mListener) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamReady(file);
                    }
                });
            }
        }

        @Override
        public void onStreamProgress(final DownloadStatus status) {
            for (final Listener listener : mListener) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamProgress(status);
                    }
                });
            }
        }
    }

    protected static void stop() {
        sThis.stopStreaming();
    }

    private Foreground.Listener mForegroundListener = new Foreground.Listener() {
        @Override
        public void onBecameForeground() {

        }

        @Override
        public void onBecameBackground() {
            if (!mIsStreaming) {
                pause();
            } else {
                startForeground();
            }
        }
    };

    private TorrentAlertListener mAlertListener = new TorrentAlertListener() {
        @Override
        public void torrentAdded(TorrentAddedAlert alert) {
            super.torrentAdded(alert);
            mCurrentTorrent = new Torrent(alert.getHandle());
            mCurrentTorrent.setListener(new TorrentListener());
            mTorrentSession.addListener(mCurrentTorrent);

            mCurrentTorrent.prepareTorrent();

            Timber.d("Video location: %s", mCurrentTorrent.getVideoFile());
        }
    };

}

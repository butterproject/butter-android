/*
 * This file is part of Butter.
 *
 * Butter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Butter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Butter. If not, see <http://www.gnu.org/licenses/>.
 */

package butter.droid.base.torrent;

import static butter.droid.base.ButterApplication.getAppContext;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import butter.droid.base.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.foreground.ForegroundListener;
import butter.droid.base.manager.internal.foreground.ForegroundManager;
import butter.droid.base.ui.TorrentActivity;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.inject.Inject;
import timber.log.Timber;

public class TorrentService extends Service implements TorrentListener {

    public static final Integer NOTIFICATION_ID = 3423423;

    private static String WAKE_LOCK = "TorrentService_WakeLock";

    private static TorrentService sThis;

    @Inject PreferencesHandler preferencesHandler;
    @Inject ForegroundManager foregroundManager;

    private TorrentStream torrentStream;
    private Torrent mCurrentTorrent;
    private StreamStatus mStreamStatus;

    private boolean mIsReady = false;
    private boolean mStopped = false;

    private IBinder mBinder = new ServiceBinder();
    private List<TorrentListener> mListener = new ArrayList<>();

    private PowerManager.WakeLock mWakeLock;
    private Class mCurrentActivityClass;
    private Timer mUpdateTimer;

    public class ServiceBinder extends Binder {

        public TorrentService getService() {
            return TorrentService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        getAppContext()
                .getComponent()
                .inject(this);

        sThis = this;
        foregroundManager.setListener(foregroundListener);

        final TorrentOptions.Builder builder = new TorrentOptions.Builder()
                .removeFilesAfterStop(true)
                .maxConnections(preferencesHandler.getTorrentConnectionLimit())
                .maxDownloadSpeed(preferencesHandler.getTorrentDownloadLimit())
                .maxUploadSpeed(preferencesHandler.getTorrentDownloadLimit())
                .saveLocation(getStreamDir());

        if (!preferencesHandler.torrentAutomaticPort()) {
            builder.listeningPort(preferencesHandler.getTorrentListeningPort());
        }

        final TorrentOptions options = builder.build();

        torrentStream = TorrentStream.init(options);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        foregroundManager.setListener(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("onBind");

        if (foregroundManager.isInForeground()) {
            stopForeground();
        }

        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Timber.d("onRebind");

        if (foregroundManager.isInForeground()) {
            stopForeground();
        }
    }

    public void setCurrentActivity(TorrentActivity activity) {
        mCurrentActivityClass = activity.getClass();

        if (foregroundManager.isInForeground()) {
            stopForeground();
            startForeground();
        }
    }

    public void startForeground() {
        if (foregroundManager.isInForeground()) {
            return;
        }
        if (mCurrentActivityClass == null) {
            return;
        }

        Intent notificationIntent = new Intent(this, mCurrentActivityClass);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent stopIntent = new Intent();
        stopIntent.setAction(TorrentBroadcastReceiver.STOP);
        PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, TorrentBroadcastReceiver.REQUEST_CODE,
                stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action stopAction = new NotificationCompat.Action.Builder(R.drawable.abc_ic_clear_material,
                getString(R.string.stop), pendingStopIntent).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notif_logo)
                .setContentTitle(getString(R.string.app_name) + " - " + getString(R.string.running))
                .setContentText(getString(R.string.tap_to_resume))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(stopAction)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        if (mStreamStatus != null && mIsReady) {
            String downloadSpeed;
            DecimalFormat df = new DecimalFormat("#############0.00");
            if (mStreamStatus.downloadSpeed / 1024 < 1000) {
                downloadSpeed = df.format(mStreamStatus.downloadSpeed / 1024) + " KB/s";
            } else {
                downloadSpeed = df.format(mStreamStatus.downloadSpeed / (1024 * 1024)) + " MB/s";
            }
            String progress = df.format(mStreamStatus.progress);
            builder.setContentText(progress + "%, ↓" + downloadSpeed);
        }

        Notification notification = builder.build();

        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notifManager.notify(NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification);

        if (mUpdateTimer == null) {
            mUpdateTimer = new Timer();
            mUpdateTimer.scheduleAtFixedRate(new UpdateTask(), 5000, 5000);
        }
    }

    public void stopForeground() {
        stopForeground(true);
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer.purge();
            mUpdateTimer = null;
        }
    }

    public void streamTorrent(@NonNull final String torrentUrl) {
        Timber.d("streamTorrent");
        mStopped = false;

        if (torrentStream.isStreaming()) {
            return;
        }

        Timber.d("Starting streaming");

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK);
        mWakeLock.acquire();

        final TorrentOptions.Builder builder = new TorrentOptions.Builder()
                .removeFilesAfterStop(true)
                .maxConnections(preferencesHandler.getTorrentConnectionLimit())
                .maxDownloadSpeed(preferencesHandler.getTorrentDownloadLimit())
                .maxUploadSpeed(preferencesHandler.getTorrentDownloadLimit())
                .saveLocation(getStreamDir());

        if (!preferencesHandler.torrentAutomaticPort()) {
            builder.listeningPort(preferencesHandler.getTorrentListeningPort());
        }

        final TorrentOptions options = builder.build();

        torrentStream.setOptions(options);

        mIsReady = false;
        torrentStream.addListener(this);
        torrentStream.startStream(torrentUrl);
    }

    public void stopStreaming() {
        mStopped = true;
        torrentStream.removeListener(this);

        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        if (!torrentStream.isStreaming()) {
            return;
        }

        stopForeground();

        torrentStream.stopStream();
        mIsReady = false;

        Timber.d("Stopped torrent and removed files if possible");
    }

    public boolean isStreaming() {
        return torrentStream.isStreaming();
    }

    public boolean isReady() {
        return mIsReady;
    }

    public boolean checkStopped() {
        if (mStopped) {
            mStopped = false;
            return true;
        }
        return false;
    }

    public void addListener(@NonNull TorrentListener listener) {
        mListener.add(listener);
    }

    public void removeListener(@NonNull TorrentListener listener) {
        mListener.remove(listener);
    }

    public static void bindHere(Context context, ServiceConnection serviceConnection) {
        Intent torrentServiceIntent = new Intent(context, TorrentService.class);
        context.bindService(torrentServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void start(Context context) {
        Intent torrentServiceIntent = new Intent(context, TorrentService.class);
        context.startService(torrentServiceIntent);
    }

    protected static void stop() {
        sThis.stopStreaming();
    }

    public Torrent getCurrentTorrent() {
        return mCurrentTorrent;
    }

    public String getCurrentTorrentUrl() {
        return torrentStream.getCurrentTorrentUrl();
    }

    @Override
    public void onStreamPrepared(Torrent torrent) {
        mCurrentTorrent = torrent;

        for (TorrentListener listener : mListener) {
            listener.onStreamPrepared(torrent);
        }
    }

    @Override
    public void onStreamStarted(Torrent torrent) {
        for (TorrentListener listener : mListener) {
            listener.onStreamStarted(torrent);
        }
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
        for (TorrentListener listener : mListener) {
            listener.onStreamError(torrent, e);
        }
    }

    @Override
    public void onStreamReady(Torrent torrent) {
        mCurrentTorrent = torrent;
        mIsReady = true;

        for (TorrentListener listener : mListener) {
            listener.onStreamReady(torrent);
        }
    }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus streamStatus) {
        for (TorrentListener listener : mListener) {
            if (null != listener) {
                listener.onStreamProgress(torrent, streamStatus);
            }
        }

        if (foregroundManager.isInForeground()) {
            mStreamStatus = streamStatus;
        }
    }

    @Override
    public void onStreamStopped() {
        for (TorrentListener listener : mListener) {
            if (listener != null) {
                listener.onStreamStopped();
            }
        }
    }

    private String getStreamDir() {
        File path = new File(preferencesHandler.getStorageLocation());
        File directory = new File(path, "/torrents/");
        return directory.toString();
    }

    private class UpdateTask extends TimerTask {

        @Override
        public void run() {
            if (foregroundManager.isInForeground()) {
                startForeground();
            } else {
                stopForeground();
            }
        }
    }

    private ForegroundListener foregroundListener = new ForegroundListener() {
        @Override public void isInForeground(final boolean inForeground) {
            if (inForeground) {
                if (!torrentStream.isStreaming()) {
                    torrentStream.resumeSession();
                } else {
                    stopForeground();
                }
            } else {
                if (!torrentStream.isStreaming()) {
                    torrentStream.pauseSession();
                } else {
                    startForeground();
                }
            }
        }
    };

}

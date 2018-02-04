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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Action;
import android.support.v4.app.NotificationCompat.Action.Builder;
import butter.droid.base.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.manager.internal.foreground.ForegroundListener;
import butter.droid.base.manager.internal.foreground.ForegroundManager;
import butter.droid.base.manager.internal.notification.ButterNotificationManager;
import butter.droid.base.utils.StringUtils;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import dagger.android.DaggerService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.inject.Inject;

public class TorrentService extends DaggerService implements TorrentListener {

    public static final int NOTIFICATION_ID = 3423423;
    private static final String WAKE_LOCK = "TorrentService_WakeLock";

    private static TorrentService sThis;

    @Inject PreferencesHandler preferencesHandler;
    @Inject ForegroundManager foregroundManager;
    @Inject PowerManager powerManager;
    @Inject PackageManager packageManager;
    @Inject ButterNotificationManager notificationManager;

    private TorrentStream torrentStream;
    private Torrent currentTorrent;
    private StreamStatus streamStatus;

    private boolean isReady = false;
    private boolean stopped = false;

    private IBinder binder = new ServiceBinder();
    private List<TorrentListener> listener = new ArrayList<>();

    private PowerManager.WakeLock wakeLock;
    private Timer updateTimer;
    //    private Class currentActivityClass; // TODO This does not respect intent data required per screen

    public class ServiceBinder extends Binder {
        public TorrentService getService() {
            return TorrentService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        foregroundManager.setListener(null);
        releaseWakeLock();
        stopUpdateTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public void streamTorrent(@NonNull final String torrentUrl) {
        stopped = false;

        if (torrentStream.isStreaming()) {
            return;
        }

        releaseWakeLock();
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK);
        wakeLock.acquire();

        final TorrentOptions.Builder builder = new TorrentOptions.Builder()
                .removeFilesAfterStop(true)
                .maxConnections(preferencesHandler.getTorrentConnectionLimit())
                .maxDownloadSpeed(preferencesHandler.getTorrentDownloadLimit())
                .maxUploadSpeed(preferencesHandler.getTorrentDownloadLimit())
                .saveLocation(getStreamDir());

        if (!preferencesHandler.torrentAutomaticPort()) {
            builder.listeningPort(preferencesHandler.getTorrentListeningPort());
        }

        torrentStream.setOptions(builder.build());

        isReady = false;
        torrentStream.addListener(this);
        torrentStream.startStream(torrentUrl);

        startForeground();
    }

    public void stopStreaming() {
        stopped = true;
        torrentStream.removeListener(this);

        releaseWakeLock();
        if (!torrentStream.isStreaming()) {
            return;
        }

        stopForeground();

        torrentStream.stopStream();
        isReady = false;

    }

    public boolean isStreaming() {
        return torrentStream.isStreaming();
    }

    public boolean isReady() {
        return isReady;
    }

    public boolean checkStopped() {
        if (stopped) {
            stopped = false;
            return true;
        }
        return false;
    }

    public void addListener(@NonNull TorrentListener listener) {
        this.listener.add(listener);
    }

    public void removeListener(@NonNull TorrentListener listener) {
        this.listener.remove(listener);
    }

    public Torrent getCurrentTorrent() {
        return currentTorrent;
    }

    public String getCurrentTorrentUrl() {
        return torrentStream.getCurrentTorrentUrl();
    }

    @Override
    public void onStreamPrepared(Torrent torrent) {
        currentTorrent = torrent;

        for (TorrentListener listener : listener) {
            listener.onStreamPrepared(torrent);
        }
    }

    @Override
    public void onStreamStarted(Torrent torrent) {
        for (TorrentListener listener : listener) {
            listener.onStreamStarted(torrent);
        }
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
        for (TorrentListener listener : listener) {
            listener.onStreamError(torrent, e);
        }
    }

    @Override
    public void onStreamReady(Torrent torrent) {
        currentTorrent = torrent;
        isReady = true;

        for (TorrentListener listener : listener) {
            listener.onStreamReady(torrent);
        }
    }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus streamStatus) {
        for (TorrentListener listener : listener) {
            if (null != listener) {
                listener.onStreamProgress(torrent, streamStatus);
            }
        }

        this.streamStatus = streamStatus;
    }

    @Override
    public void onStreamStopped() {
        for (TorrentListener listener : listener) {
            if (listener != null) {
                listener.onStreamStopped();
            }
        }
    }

    private void startForeground() {
        Intent notificationIntent = packageManager.getLaunchIntentForPackage(getPackageName());
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && notificationIntent == null) {
            notificationIntent = packageManager.getLeanbackLaunchIntentForPackage(getPackageName());
        }

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent();
        stopIntent.setAction(TorrentBroadcastReceiver.STOP);
        PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, TorrentBroadcastReceiver.REQUEST_CODE,
                stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Action stopAction = new Builder(R.drawable.ic_clear,
                getString(R.string.stop), pendingStopIntent).build();

        // TODO text to resources
        notificationManager.createChannel(ButterNotificationManager.CHANNEL_STREAMING, "Straming", NotificationManager.IMPORTANCE_LOW);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ButterNotificationManager.CHANNEL_STREAMING)
                .setSmallIcon(R.drawable.ic_notif_logo)
                .setContentTitle(getString(R.string.app_name) + " - " + getString(R.string.running))
                .setContentText(getString(R.string.tap_to_resume))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .addAction(stopAction)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        if (streamStatus != null && isReady) {
            String downloadSpeed = StringUtils.formatSpeed(streamStatus.downloadSpeed);
            builder.setContentText((int) streamStatus.progress + "%, ↓" + downloadSpeed);
        }

        startForeground(NOTIFICATION_ID, builder.build());

        if (updateTimer == null) {
            updateTimer = new Timer();
            updateTimer.scheduleAtFixedRate(new UpdateTask(), 5000, 5000);
        }
    }

    private void stopForeground() {
        stopForeground(true);
        stopUpdateTimer();
    }

    private String getStreamDir() {
        File path = new File(preferencesHandler.getStorageLocation());
        File directory = new File(path, "/torrents/");
        return directory.toString();
    }

    private void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer.purge();
            updateTimer = null;
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public static void bindHere(Context context, ServiceConnection serviceConnection) {
        Intent torrentServiceIntent = new Intent(context, TorrentService.class);
        context.bindService(torrentServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, TorrentService.class);
        context.startService(intent);
    }

    public static void stop() {
        sThis.stopStreaming();
    }

    private class UpdateTask extends TimerTask {

        @Override
        public void run() {
            // TODO update notification
            //            if (foregroundManager.isInForeground()) {
//                startForeground();
//            } else {
//                stopForeground();
//            }
        }
    }

    private ForegroundListener foregroundListener = new ForegroundListener() {
        @Override public void isInForeground(final boolean inForeground) {
            if (inForeground) {
                startForeground();
                if (!torrentStream.isStreaming()) {
                    torrentStream.resumeSession();
                }
            } else {
                if (!torrentStream.isStreaming()) {
                    torrentStream.pauseSession();
                    stopSelf();
                } else {
                    startForeground();
                }
            }
        }
    };

}

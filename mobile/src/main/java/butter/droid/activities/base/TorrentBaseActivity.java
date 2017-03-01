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

package butter.droid.activities.base;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.github.sv244.torrentstream.StreamStatus;
import com.github.sv244.torrentstream.Torrent;
import com.github.sv244.torrentstream.listeners.TorrentListener;

import javax.inject.Inject;

import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.torrent.TorrentService;
import butter.droid.base.ui.TorrentActivity;
import butter.droid.base.utils.LocaleUtils;
import butterknife.ButterKnife;
import retrofit.http.HEAD;

public abstract class TorrentBaseActivity extends AppCompatActivity implements TorrentListener, TorrentActivity {

    @Inject PreferencesHandler preferencesHandler;

    protected Handler torrentHandler;
    protected TorrentService torrentStream;

    protected void onCreate(Bundle savedInstanceState, int layoutId) {
        String language = preferencesHandler.getLocale();
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.onCreate(savedInstanceState);

        if (layoutId != 0) {
            setContentView(layoutId);
            ButterKnife.bind(this);
        }

        torrentHandler = new Handler(getMainLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();
        TorrentService.bindHere(this, mServiceConnection);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != torrentStream) {
            unbindService(mServiceConnection);
            torrentStream = null;
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        String language = preferencesHandler.getLocale();
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.setContentView(layoutResID);
    }

    protected ButterApplication getApp() {
        return (ButterApplication) getApplication();
    }

    public TorrentService getTorrentService() {
        return torrentStream;
    }

    public void onTorrentServiceConnected() {
        // Placeholder
    }

    public void onTorrentServiceDisconnected() {
        // Placeholder
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            torrentStream = ((TorrentService.ServiceBinder) service).getService();
            torrentStream.addListener(TorrentBaseActivity.this);
            torrentStream.setCurrentActivity(TorrentBaseActivity.this);
            onTorrentServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            torrentStream.removeListener(TorrentBaseActivity.this);
            torrentStream = null;
            onTorrentServiceDisconnected();
        }
    };

    @Override
    public void onStreamPrepared(Torrent torrent) {

    }

    @Override
    public void onStreamStarted(Torrent torrent) {

    }

    @Override
    public void onStreamReady(Torrent torrent) {

    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {

    }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus streamStatus) {

    }

    @Override
    public void onStreamStopped() {

    }
}

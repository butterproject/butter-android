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

package butter.droid.tv.ui;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.torrent.TorrentService;
import butter.droid.base.ui.TorrentActivity;
import butter.droid.base.utils.LocaleUtils;
import butterknife.ButterKnife;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasFragmentInjector;
import dagger.android.support.HasSupportFragmentInjector;
import javax.inject.Inject;

public abstract class TVTorrentBaseActivity extends FragmentActivity implements TorrentListener, TorrentActivity, ServiceConnection,
        HasFragmentInjector, HasSupportFragmentInjector {

    @Inject DispatchingAndroidInjector<Fragment> fragmentInjector;
    @Inject DispatchingAndroidInjector<android.support.v4.app.Fragment> supportFragmentInjector;
    @Inject PreferencesHandler preferencesHandler;

    protected TorrentService torrentStream;

    protected void onCreate(Bundle savedInstanceState, int layoutId) {
        AndroidInjection.inject(this);
        String language = preferencesHandler.getLocale();
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.onCreate(savedInstanceState);

        if (layoutId != 0) {
            setContentView(layoutId);
            ButterKnife.bind(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TorrentService.bindHere(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != torrentStream) {
            torrentStream.removeListener(this);
            unbindService(this);
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

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        torrentStream = ((TorrentService.ServiceBinder) service).getService();
        torrentStream.addListener(this);
        onTorrentServiceConnected(torrentStream);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        torrentStream.removeListener(this);
        onTorrentServiceDisconnected(torrentStream);
        torrentStream = null;
    }

    public void onTorrentServiceConnected(final TorrentService service) {
        // Placeholder
    }

    public void onTorrentServiceDisconnected(final TorrentService service) {
        // Placeholder
    }

    @Override
    public void onStreamPrepared(Torrent torrent) {

    }

    @Override
    public void onStreamStarted(Torrent torrent) {

    }

    @Override
    public void onStreamError(Torrent torrent, Exception ex) {

    }

    @Override
    public void onStreamReady(Torrent torrent) {

    }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus streamStatus) {

    }

    @Override
    public void onStreamStopped() {

    }

    @Override
    public AndroidInjector<Fragment> fragmentInjector() {
        return fragmentInjector;
    }

    @Override public AndroidInjector<android.support.v4.app.Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }
}

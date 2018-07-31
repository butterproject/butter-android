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

package butter.droid.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.torrent.TorrentService;
import butter.droid.base.ui.TorrentActivity;
import butter.droid.base.utils.LocaleUtils;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.DaggerAppCompatActivity;
import dagger.android.support.HasSupportFragmentInjector;

import static android.os.Looper.getMainLooper;

public abstract class TorrentBaseActivity extends DaggerAppCompatActivity implements TorrentActivity,
        HasSupportFragmentInjector {

    static {
        // TODO should be removed
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Inject DispatchingAndroidInjector<Fragment> supportFragmentInjector;
    @Inject PreferencesHandler preferencesHandler;

    protected Handler torrentHandler;
    private TorrentService torrentStream;

    @Override protected void onCreate(Bundle savedInstanceState) {
        String language = preferencesHandler.getLocale();
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));

        super.onCreate(savedInstanceState);

        torrentHandler = new Handler(getMainLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();
        TorrentService.bindHere(this, serviceConnection);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != torrentStream) {
            unbindService(serviceConnection);
            serviceConnection.onServiceDisconnected(null);
            torrentStream = null;
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        String language = preferencesHandler.getLocale();
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.setContentView(layoutResID);
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return supportFragmentInjector;
    }

    protected ButterApplication getApp() {
        return (ButterApplication) getApplication();
    }

    public TorrentService getTorrentService() {
        return torrentStream;
    }

    public void onTorrentServiceConnected(final TorrentService service) {
        // Placeholder
    }

    public void onTorrentServiceDisconnected(final TorrentService service) {
        // Placeholder
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            torrentStream = ((TorrentService.ServiceBinder) service).getService();
            onTorrentServiceConnected(torrentStream);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            onTorrentServiceDisconnected(torrentStream);
            torrentStream = null;
        }
    };

}

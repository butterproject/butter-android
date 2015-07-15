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

package pct.droid.tv.activities.base;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

import java.io.File;

import butterknife.ButterKnife;
import pct.droid.base.PopcornApplication;
import pct.droid.base.activities.TorrentActivity;
import pct.droid.base.preferences.Prefs;
import pct.droid.base.torrent.DownloadStatus;
import pct.droid.base.torrent.TorrentService;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PrefUtils;

public abstract class PTVTorrentBaseActivity extends FragmentActivity implements TorrentService.Listener, TorrentActivity {

    protected Handler mHandler;
    protected TorrentService mService;

    protected void onCreate(Bundle savedInstanceState, int layoutId) {
        String language = PrefUtils.get(this, Prefs.LOCALE, PopcornApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.onCreate(savedInstanceState);
        setContentView(layoutId);
        ButterKnife.bind(this);
        mHandler = new Handler(getMainLooper());
    }

    @Override
    protected void onResume() {
        super.onResume();
        TorrentService.bindHere(this, mServiceConnection);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mService)
            unbindService(mServiceConnection);
    }

    @Override
    public void setContentView(int layoutResID) {
        String language = PrefUtils.get(this, Prefs.LOCALE, PopcornApplication.getSystemLanguage());
        LocaleUtils.setCurrent(this, LocaleUtils.toLocale(language));
        super.setContentView(layoutResID);
    }

    protected PopcornApplication getApp() {
        return (PopcornApplication) getApplication();
    }

    public TorrentService getTorrentService() {
        return mService;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((TorrentService.ServiceBinder) service).getService();
            mService.addListener(PTVTorrentBaseActivity.this);
            onTorrentServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.removeListener(PTVTorrentBaseActivity.this);
            mService = null;
            onTorrentServiceDisconnected();
        }
    };

    public void onTorrentServiceConnected() {
        // Placeholder
    }

    public void onTorrentServiceDisconnected() {
        // Placeholder
    }

    @Override
    public void onStreamStarted() {

    }

    @Override
    public void onStreamError(Exception e) {

    }

    @Override
    public void onStreamReady(File videoLocation) {

    }

    @Override
    public void onStreamProgress(DownloadStatus status) {

    }
}

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

package butter.droid.base.manager.internal.foreground;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import butter.droid.base.Internal;
import javax.inject.Inject;

@Internal
public class ForegroundManager implements ComponentCallbacks2, ActivityLifecycleCallbacks {

    private boolean isInForeground;
    private ForegroundListener listener;

    @Inject
    public ForegroundManager(final Application application) {
        application.registerActivityLifecycleCallbacks(this);
        application.registerComponentCallbacks(this);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        application.registerReceiver(new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                setInForeground(false);
            }
        }, filter);
    }

    public void setListener(final ForegroundListener listener) {
        this.listener = listener;
    }

    public boolean isInForeground() {
        return isInForeground;
    }

    protected void setInForeground(boolean inForeground) {
        if (isInForeground != inForeground) {
            isInForeground = inForeground;
            if (listener != null) {
                listener.isInForeground(isInForeground);
            }
        }
    }

    // MEMORY

    @Override public void onTrimMemory(final int level) {
        // do greater or equals comparison, because system may skip callback with level TRIM_MEMORY_UI_HIDDEN
        if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            setInForeground(false);
        }
    }

    @Override public void onConfigurationChanged(final Configuration newConfig) {
        // nothing to do
    }

    @Override public void onLowMemory() {
        // nothing to do
    }

    // LIFECYCLE

    @Override public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
        // nothing to do
    }

    @Override public void onActivityStarted(final Activity activity) {
        // nothing to do
    }

    @Override public void onActivityResumed(final Activity activity) {
        setInForeground(true);
    }

    @Override public void onActivityPaused(final Activity activity) {
        // nothing to do
    }

    @Override public void onActivityStopped(final Activity activity) {
        // nothing to do
    }

    @Override public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
        // nothing to do
    }

    @Override public void onActivityDestroyed(final Activity activity) {
        // nothing to do
    }
}

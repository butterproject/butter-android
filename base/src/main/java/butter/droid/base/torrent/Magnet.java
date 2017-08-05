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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;

import butter.droid.base.providers.model.StreamInfo;
import java.util.ArrayList;
import java.util.List;

import butter.droid.base.R;

public class Magnet {

    private Context context;
    private boolean canOpen = false;
    private Intent openIntent;

    public Magnet(Context context, String magnetUrl) {
        this.context = context;
        setUrl(magnetUrl);
    }

    public Magnet(Context context, StreamInfo streamInfo) {
        this(context, streamInfo.getTorrentUrl());
    }

    public void setUrl(String magnetUrl) {
        if(magnetUrl == null) {
            canOpen = false;
            return;
        }

        Uri uri = Uri.parse(magnetUrl);

        List<Intent> filteredShareIntents = new ArrayList<>();
        Intent torrentIntent = new Intent(Intent.ACTION_VIEW, uri);
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(torrentIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo info : resolveInfoList) {
            if (!info.activityInfo.packageName.contains("pct.droid")) {     // Black listing the app its self
                Intent targetedShare = new Intent(Intent.ACTION_VIEW, uri);
                targetedShare.setPackage(info.activityInfo.packageName);
                filteredShareIntents.add(targetedShare);
            }
        }

        if (filteredShareIntents.size() > 0){
            Intent filteredIntent = Intent.createChooser(filteredShareIntents.remove(0), context.getString(R.string.open_with));
            filteredIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, filteredShareIntents.toArray(new Parcelable[filteredShareIntents.size()]));
            openIntent = filteredIntent;
            canOpen = true;
        } else {
            canOpen = false;
        }
    }

    public void open(Activity activity) {
        if(openIntent != null) {
            activity.startActivity(openIntent);
        }
    }

    public boolean canOpen() {
        return canOpen;
    }

}

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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pct.droid.base.R;

public class Magnet {

    private Context mContext;
    private boolean mCanOpen = false;
    private Intent mOpenIntent;

    public Magnet(Context context, String magnetUrl) {
        mContext = context;
        setUrl(magnetUrl);
    }

    public Magnet(Context context, StreamInfo streamInfo) {
        this(context, streamInfo.getTorrentUrl());
    }

    public void setUrl(String magnetUrl) {
        if(magnetUrl == null) {
            mCanOpen = false;
            return;
        }

        Uri uri = Uri.parse(magnetUrl);

        List<Intent> filteredShareIntents = new ArrayList<>();
        Intent torrentIntent = new Intent(Intent.ACTION_VIEW, uri);
        List<ResolveInfo> resolveInfoList = mContext.getPackageManager().queryIntentActivities(torrentIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo info : resolveInfoList) {
            if (!info.activityInfo.packageName.contains("pct.droid")) {     // Black listing the app its self
                Intent targetedShare = new Intent(Intent.ACTION_VIEW, uri);
                targetedShare.setPackage(info.activityInfo.packageName);
                filteredShareIntents.add(targetedShare);
            }
        }

        if (filteredShareIntents.size() > 0){
            Intent filteredIntent = Intent.createChooser(filteredShareIntents.remove(0), mContext.getString(R.string.open_with));
            filteredIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, filteredShareIntents.toArray(new Parcelable[filteredShareIntents.size()]));
            mOpenIntent = filteredIntent;
            mCanOpen = true;
        } else {
            mCanOpen = false;
        }
    }

    public void open(Activity activity) {
        if(mOpenIntent != null) {
            activity.startActivity(mOpenIntent);
        }
    }

    public boolean canOpen() {
        return mCanOpen;
    }

}

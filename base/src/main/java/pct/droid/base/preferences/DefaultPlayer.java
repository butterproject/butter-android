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

package pct.droid.base.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pct.droid.base.PopcornApplication;
import pct.droid.base.beaming.server.BeamServer;
import pct.droid.base.beaming.server.BeamServerService;
import pct.droid.base.providers.media.models.Episode;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.utils.LocaleUtils;
import pct.droid.base.utils.PrefUtils;

public class DefaultPlayer {

    private static String DELIMITER = "/-/";

    /**
     * Get all available video player applications
     *
     * @return Map with options
     */
    public static Map<String, String> getVideoPlayerApps() {
        Intent playerIntent = new Intent(Intent.ACTION_VIEW);
        playerIntent.setDataAndType(Uri.parse("http://get.popcorntime.io/nwtests/bbb_sunflower_1080p_30fps_normal_aac.mp4"), "video/*");

        PackageManager packageManager = PopcornApplication.getAppContext().getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(playerIntent, 0);

        HashMap<String, String> returnMap = new HashMap<>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            returnMap.put(resolveInfo.activityInfo.name + DELIMITER + resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.applicationInfo.loadLabel(packageManager).toString());
        }

        return returnMap;
    }

    /**
     * Set default player
     *
     * @param playerName Name of video player
     * @param playerData Data of videoplayer (format: activity name + DELIMITER + package name)
     */
    public static void set(String playerName, String playerData) {
        if (playerName.isEmpty() || playerData.isEmpty()) {
            PrefUtils.remove(PopcornApplication.getAppContext(), Prefs.DEFAULT_PLAYER);
            PrefUtils.remove(PopcornApplication.getAppContext(), Prefs.DEFAULT_PLAYER_NAME);
            return;
        }

        PrefUtils.save(PopcornApplication.getAppContext(), Prefs.DEFAULT_PLAYER_NAME, playerName);
        PrefUtils.save(PopcornApplication.getAppContext(), Prefs.DEFAULT_PLAYER, playerData);
    }

    /**
     * Start default video player if set, otherwise return {@code false} so that the application can handle the video itself
     *
     * @param location Video location
     * @return {@code true} if activity started, {@code false} otherwise
     */
    public static boolean start(Media media, String subLanguage, String location) {
        Context context = PopcornApplication.getAppContext();
        String[] playerData = PrefUtils.get(context, Prefs.DEFAULT_PLAYER, "").split(DELIMITER);
        if (playerData.length > 1) {
            Intent intent = new Intent();
            if (null != media && media.subtitles != null && media.subtitles.size() > 0 && subLanguage != null && !subLanguage.equals("no-subs")) {
                File subsLocation = new File(SubsProvider.getStorageLocation(context), media.videoId + "-" + subLanguage + ".srt");
                BeamServer.setCurrentSubs(subsLocation);
                intent.putExtra("subs", new Uri[] { Uri.parse(BeamServer.getSubsURL()) });
                intent.putExtra("subs.name", new String[] { LocaleUtils.toLocale(subLanguage).getDisplayLanguage() });
            }

            BeamServer.setCurrentVideo(location);
            BeamServerService.getServer().start();

            intent.setClassName(playerData[1], playerData[0]);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(BeamServer.getVideoURL()), "video/mp4");

            if(media != null) {
                if(media.isMovie) {
                    intent.putExtra("title", media.title);
                } else {
                    Episode episode = (Episode) media;
                    intent.putExtra("title", String.format("%s S%dE%d - %s", episode.showName, episode.season, episode.episode, episode.title));
                }
            }
            intent.putExtra("position", 0);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

}

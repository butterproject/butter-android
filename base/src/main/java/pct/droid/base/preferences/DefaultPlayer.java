package pct.droid.base.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pct.droid.base.PopcornApplication;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.subs.SubsProvider;
import pct.droid.base.utils.FileUtils;
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
     * @param context  Context
     * @param location Video location
     * @return {@code true} if activity started, {@code false} otherwise
     */
    public static boolean start(Context context, Media media, String subLanguage, String location) {
        String[] playerData = PrefUtils.get(context, Prefs.DEFAULT_PLAYER, "").split(DELIMITER);
        if (playerData.length > 1) {
            if (media.subtitles.size() > 0) {
                File subsLocation = new File(SubsProvider.getStorageLocation(context), media.videoId + "-" + subLanguage + ".srt");
                File newLocation = new File(location.replace("." + FileUtils.getFileExtension(location), ".srt"));

                if (subLanguage != null && !subLanguage.equals("no-subs")) {
                    try {
                        newLocation.getParentFile().mkdirs();
                        newLocation.createNewFile();
                        FileUtils.copy(subsLocation, newLocation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    newLocation.delete();
                }
            }

            Intent intent = new Intent();
            intent.setClassName(playerData[1], playerData[0]);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + location), "video/mp4");
            context.startActivity(intent);
            return true;
        }
        return false;
    }

}

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

package butter.droid.base.manager.vlc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.manager.beaming.server.BeamServer;
import butter.droid.base.manager.beaming.server.BeamServerService;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.providers.media.models.Episode;
import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.subs.SubsProvider;
import butter.droid.base.utils.LocaleUtils;
import butter.droid.base.utils.StringUtils;

public class PlayerManager {

    private static final String DELIMITER = "/-/";

    private final PreferencesHandler preferencesHandler;
    private final PrefManager prefManager;

    @Inject
    public PlayerManager(PreferencesHandler preferencesHandler, PrefManager prefManager) {
        this.preferencesHandler = preferencesHandler;
        this.prefManager = prefManager;
    }

    /**
     * Get all available video player applications
     *
     * @return Map with options
     */
    public Map<String, String> getVideoPlayerApps() {
        Intent playerIntent = new Intent(Intent.ACTION_VIEW);
        playerIntent.setDataAndType(
                Uri.parse("http://get.popcorntime.io/nwtests/bbb_sunflower_1080p_30fps_normal_aac.mp4"), "video/*");

        PackageManager packageManager = ButterApplication.getAppContext().getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(playerIntent, 0);

        HashMap<String, String> returnMap = new HashMap<>();
        for (ResolveInfo resolveInfo : resolveInfoList) {
            returnMap.put(resolveInfo.activityInfo.name + DELIMITER + resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.applicationInfo.loadLabel(packageManager).toString());
        }

        playerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("/path/video.mp4"));
        playerIntent.setDataAndType(Uri.parse("/path/video.mp4"), "video/mp4");

        resolveInfoList = packageManager.queryIntentActivities(playerIntent, 0);

        for (ResolveInfo resolveInfo : resolveInfoList) {
            returnMap.put(resolveInfo.activityInfo.name + DELIMITER + resolveInfo.activityInfo.packageName,
                    resolveInfo.activityInfo.applicationInfo.loadLabel(packageManager).toString());
        }

        return returnMap;
    }

    /**
     * Set default player
     *
     * @param playerName Name of video player
     * @param playerData Data of videoplayer (format: activity name + DELIMITER + package name)
     */
    public void set(@Nullable String playerName, @Nullable String playerData) {
        if (StringUtils.isEmpty(playerName) || StringUtils.isEmpty(playerData)) {
            prefManager.remove(Prefs.DEFAULT_PLAYER);
            prefManager.remove(Prefs.DEFAULT_PLAYER_NAME);
        } else {
            prefManager.save(Prefs.DEFAULT_PLAYER_NAME, playerName);
            prefManager.save(Prefs.DEFAULT_PLAYER, playerData);
        }
    }

    /**
     * Start default video player if set, otherwise return {@code false} so that the application can handle the video itself
     *
     * @param location Video location
     * @return {@code true} if activity started, {@code false} otherwise
     */
    public boolean start(Media media, String subLanguage, String location) {
        Context context = ButterApplication.getAppContext();
        String defaultPlayer = preferencesHandler.getDefaultPlayer();

        if (!StringUtils.isEmpty(defaultPlayer)) {
            String[] playerData = defaultPlayer.split(DELIMITER);
            if (playerData.length > 1) {
                Intent intent = new Intent();
                if (null != media && media.subtitles != null && media.subtitles.size() > 0 && subLanguage != null && !subLanguage.equals(
                        "no-subs")) {
                    File subsLocation = new File(getStorageLocation(), media.videoId + "-" + subLanguage + ".srt");
                    BeamServer.setCurrentSubs(subsLocation);
                    intent.putExtra("subs", new Uri[]{Uri.parse(BeamServer.getSubsURL())});
                    intent.putExtra("subs.name", new String[]{LocaleUtils.toLocale(subLanguage).getDisplayLanguage()});
                }

                BeamServer.setCurrentVideo(location);
                BeamServerService.getServer().start();

                intent.setClassName(playerData[1], playerData[0]);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(BeamServer.getVideoURL()), "video/mp4");

                if (media != null) {
                    if (media.isMovie) {
                        intent.putExtra("title", media.title);
                    } else {
                        Episode episode = (Episode) media;
                        intent.putExtra("title",
                                String.format("%s S%dE%d - %s", episode.showName, episode.season, episode.episode,
                                        episode.title));
                    }
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }

    public String getDefaultQuality(List<String> availableQualities) {
        String quality = preferencesHandler.getDefaultQuality();
        String[] fallbackOrder = new String[]{"720p", "480p", "1080p"};

        if (availableQualities.indexOf(quality) == -1) {
            for (String fallbackQuality : fallbackOrder) {
                if (availableQualities.indexOf(fallbackQuality) != -1) {
                    quality = fallbackQuality;
                    break;
                }
            }
        }

        return quality;
    }

    public File getStorageLocation() {
        return new File(preferencesHandler.getStorageLocation() + "/subs/");
    }

    public File getDownloadedSubtitleFile(@NonNull Media media, @NonNull String language) throws FileNotFoundException {
        if (language.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
            throw new IllegalArgumentException("language must be specified");
        }

        File subtitleFile = new File(getStorageLocation(), media.videoId + "-" + language + ".srt");

        if (subtitleFile.exists()) {
            return subtitleFile;
        }

        throw new FileNotFoundException("Subtitle file does not exists");
    }

}

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

package butter.droid.base.manager.internal.vlc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import butter.droid.base.ButterApplication;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.base.content.preferences.Prefs;
import butter.droid.base.manager.internal.beaming.server.BeamServer;
import butter.droid.base.manager.internal.beaming.server.BeamServerService;
import butter.droid.base.manager.prefs.PrefManager;
import butter.droid.base.providers.model.StreamInfo;
import butter.droid.base.utils.StringUtils;
import butter.droid.provider.base.module.Media;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

public class PlayerManager {

    private static final Uri FAKE_HTTP_VIDEO_URI = Uri.parse("http://butterproject.org/test.mp4");
    private static final Uri FAKE_LOCAL_VIDEO_URI = Uri.parse("/path/video.mp4");

    private static final String DELIMITER = "/-/";

    private final PackageManager packageManager;
    private final PreferencesHandler preferencesHandler;
    private final PrefManager prefManager;

    @Inject
    public PlayerManager(PackageManager packageManager, PreferencesHandler preferencesHandler, PrefManager prefManager) {
        this.packageManager = packageManager;
        this.preferencesHandler = preferencesHandler;
        this.prefManager = prefManager;
    }

    /**
     * Get all available video player applications
     *
     * @return Map with options
     */
    public Map<String, String> getVideoPlayerApps() {
        final Intent httpVideoIntent = new Intent(Intent.ACTION_VIEW);
        httpVideoIntent.setDataAndType(FAKE_HTTP_VIDEO_URI, "video/*");

        final Intent localVideoIntent = new Intent(Intent.ACTION_VIEW, FAKE_LOCAL_VIDEO_URI);
        localVideoIntent.setDataAndType(FAKE_LOCAL_VIDEO_URI, "video/mp4");

        final List<ResolveInfo> remoteInfoList = packageManager.queryIntentActivities(httpVideoIntent, 0);
        final List<ResolveInfo> localInfoList = packageManager.queryIntentActivities(localVideoIntent, 0);

        final Set<ResolveInfo> resultInfoList = new HashSet<>();
        resultInfoList.addAll(remoteInfoList);
        resultInfoList.addAll(localInfoList);

        final HashMap<String, String> returnMap = new HashMap<>();

        for (ResolveInfo resolveInfo : resultInfoList) {
            final String applicationName = resolveInfo.activityInfo.applicationInfo.loadLabel(packageManager).toString();
            final String playerActivity = resolveInfo.activityInfo.name;
            final String playerPackageName = resolveInfo.activityInfo.packageName;

            if (!playerPackageName.startsWith("butter.droid")) {
                returnMap.put(playerActivity + DELIMITER + playerPackageName, applicationName);
            }
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

    @Nullable public Intent externalPlayerIntent(@NonNull StreamInfo streamInfo) {

        final String defaultPlayer = preferencesHandler.getDefaultPlayer();

        if (!StringUtils.isEmpty(defaultPlayer)) {
            String[] playerData = defaultPlayer.split(DELIMITER);
            if (playerData.length > 1) {
                Intent intent = new Intent();
                // TODO: 11/1/17 Subtitles
                //                if (null != media && media.subtitles != null && media.subtitles.size() > 0 && subLanguage != null && !subLanguage.equals(
//                        "no-subs")) {
//                    File subsLocation = new File(getStorageLocation(), media.getId() + "-" + subLanguage + ".srt");
//                    BeamServer.setCurrentSubs(subsLocation);
//                    intent.putExtra("subs", new Uri[]{Uri.parse(BeamServer.getSubsURL())});
//                    intent.putExtra("subs.name", new String[]{LocaleUtils.toLocale(subLanguage).getDisplayLanguage()});
//                }

                BeamServer.setCurrentVideo(streamInfo.getStreamUrl());
                BeamServerService.getServer().start();

                intent.setClassName(playerData[1], playerData[0]);
                intent.setAction(Intent.ACTION_VIEW);
                // TODO: 7/29/17 Actual mime type!
                intent.setDataAndType(Uri.parse(BeamServer.getVideoURL()), "video/mp4");
                intent.putExtra(Intent.EXTRA_TITLE, streamInfo.getFullTitle());

                return intent;
            }
        }

        return null;
    }

    /**
     * Start default video player if set, otherwise return {@code false} so that the application can handle the video itself
     *
     * @return {@code true} if activity started, {@code false} otherwise
     * @deprecated Use {@link #externalPlayerIntent(StreamInfo)} instead.
     */
    @Deprecated public boolean start(StreamInfo streamInfo) {
        Intent intent = externalPlayerIntent(streamInfo);
        if (intent != null) {
            Context context = ButterApplication.getAppContext();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } else {
            return false;
        }
    }

    public File getStorageLocation() {
        return new File(preferencesHandler.getStorageLocation() + "/subs/");
    }

    public File getDownloadedSubtitleFile(@NonNull Media media, @NonNull String language) throws FileNotFoundException {
//        if (language.equals(SubsProvider.SUBTITLE_LANGUAGE_NONE)) {
//            throw new IllegalArgumentException("language must be specified");
//        }

        File subtitleFile = new File(getStorageLocation(), media.getId() + "-" + language + ".srt");

        if (subtitleFile.exists()) {
            return subtitleFile;
        }

        throw new FileNotFoundException("Subtitle file does not exists");
    }

}

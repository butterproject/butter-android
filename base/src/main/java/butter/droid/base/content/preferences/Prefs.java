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

package butter.droid.base.content.preferences;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.StringDef;

public final class Prefs {

    // region StringDef

    @StringDef({SUBTITLE_COLOR, SUBTITLE_SIZE, SUBTITLE_STROKE_WIDTH, SUBTITLE_STROKE_COLOR, SUBTITLE_DEFAULT_LANGUAGE,
            STORAGE_LOCATION, REMOVE_CACHE, HW_ACCELERATION, AUTOMATIC_UPDATES, DEFAULT_PROVIDER, DEFAULT_PLAYER,
            DEFAULT_PLAYER_NAME, WIFI_ONLY, LOCALE, LIBTORRENT_CONNECTION_LIMIT, LIBTORRENT_DOWNLOAD_LIMIT, LIBTORRENT_UPLOAD_LIMIT,
            LIBTORRENT_LISTENING_PORT, LIBTORRENT_AUTOMATIC_PORT, SHOW_VPN, PIXEL_FORMAT, QUALITY_DEFAULT, REPORT_BUG,
            CHANGE_LOG, NOTICE, VERSION, ABOUT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PrefKey {
    }

    public static final String TITLE_GENERAL = "title_general";
    public static final String TITLE_SUBTITLES = "title_subtitles";
    public static final String TITLE_TORRENTS = "title_torrents";
    public static final String TITLE_NETWORKING = "title_networking";
    public static final String TITLE_ADVANCED = "title_advanced";
    public static final String TITLE_UPDATES = "title_updates";
    public static final String TITLE_ABOUT = "title_about";

    public static final String SUBTITLE_COLOR = "subtitle_color";
    public static final String SUBTITLE_SIZE = "subtitle_size";
    public static final String SUBTITLE_STROKE_WIDTH = "subtitle_stroke_width";
    public static final String SUBTITLE_STROKE_COLOR = "subtitle_stroke_color";
    public static final String SUBTITLE_DEFAULT_LANGUAGE = "subtitle_default_language";
    public static final String STORAGE_LOCATION = "storage_location";
    public static final String REMOVE_CACHE = "remove_cache";
    public static final String HW_ACCELERATION = "hw_acceleration"; // TODO: 12/9/16 This is never used
    public static final String AUTOMATIC_UPDATES = "auto_updates";
    public static final String DEFAULT_PROVIDER = "default_provider";
    public static final String DEFAULT_PLAYER = "default_player";
    public static final String DEFAULT_PLAYER_NAME = "default_player_name";
    public static final String WIFI_ONLY = "wifi_only";
    public static final String LOCALE = "app_locale";
    public static final String LIBTORRENT_CONNECTION_LIMIT = "libtorrent_connectionlimit";
    public static final String LIBTORRENT_DOWNLOAD_LIMIT = "libtorrent_downloadlimit";
    public static final String LIBTORRENT_UPLOAD_LIMIT = "libtorrent_uploadlimit";
    public static final String LIBTORRENT_LISTENING_PORT = "libtorrent_listeningport";
    public static final String LIBTORRENT_AUTOMATIC_PORT = "libtorrent_automaticport";
    public static final String SHOW_VPN = "show_vpn"; // TODO: 12/9/16 This is never used
    public static final String PIXEL_FORMAT = "pixel_format";
    public static final String QUALITY_DEFAULT = "quality_default";
//    public static final String CHECK_UPDATE = "check_update";
    public static final String REPORT_BUG = "report_bug";
    public static final String CHANGE_LOG = "change_log";
    public static final String NOTICE = "notice";
    public static final String VERSION = "version";
    public static final String ABOUT = "about";

    // endregion StringDef

    private Prefs() {
        // no instances
    }

}

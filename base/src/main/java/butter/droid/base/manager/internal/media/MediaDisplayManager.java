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

package butter.droid.base.manager.internal.media;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.Arrays;

import javax.inject.Inject;

import butter.droid.base.Internal;
import butter.droid.base.R;
import butter.droid.base.content.preferences.PreferencesHandler;
import butter.droid.provider.base.model.Format;
import butter.droid.provider.base.model.Torrent;

@Internal
public class MediaDisplayManager {

    private final PreferencesHandler preferencesHandler;
    private final Resources resources;

    @Inject
    public MediaDisplayManager(final PreferencesHandler preferencesHandler, final Resources resources) {
        this.preferencesHandler = preferencesHandler;
        this.resources = resources;
    }

    public String getFormatDisplayName(@NonNull Format format) {
        @StringRes int textRes;
        if (format.getType() == 0) {
            if (format.getQuality() == Format.QUALITY_4K) {
                textRes = R.string.picker_format_normal_4K;
            } else {
                textRes = R.string.picker_format_normal;
            }
        } else {
            textRes = R.string.picker_format_3D;
        }

        return resources.getString(textRes, format.getQuality());
    }

    public Torrent[] getSortedTorrents(Torrent[] torrents) {
        final Torrent[] newTorrents = new Torrent[torrents.length];
        System.arraycopy(torrents, 0, newTorrents, 0, torrents.length);
        return sortTorrentsByFormat(newTorrents);
    }

    public int getDefaultFormatIndex(Torrent[] availableTorrents) {
        int quality = preferencesHandler.getDefaultQuality();

        if (availableTorrents != null && availableTorrents.length > 0) {
            int format3D = -1;

            for (int i = 0; i < availableTorrents.length; i++) {
                Format format = availableTorrents[i].getFormat();
                if (format.getQuality() == quality) {
                    if (format.getType() == 0) {
                        return i;
                    } else if (format3D == -1) {
                        format3D = i;
                    }
                }
            }

            if (format3D > -1) {
                return format3D;
            } else {
                return 0;
            }
        } else {
            throw new IllegalStateException("No formats provided");
        }
    }

    private Torrent[] sortTorrentsByFormat(Torrent[] torrents) {
        Arrays.sort(torrents, (lhs, rhs) -> {
            int i = lhs.getFormat().getType() - rhs.getFormat().getType();
            if (i != 0) {
                return i;
            }

            return lhs.getFormat().getQuality() - rhs.getFormat().getQuality();
        });

        return torrents;
    }

}

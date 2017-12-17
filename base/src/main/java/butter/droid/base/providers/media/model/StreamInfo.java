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

package butter.droid.base.providers.media.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import butter.droid.base.providers.subs.model.SubtitleWrapper;
import butter.droid.provider.base.model.Media;
import butter.droid.provider.base.model.Torrent;
import butter.droid.provider.subs.model.Subtitle;
import java.util.Locale;
import org.parceler.Parcels;

public class StreamInfo implements Parcelable {

    @NonNull private final MediaWrapper media;
    @Nullable private final MediaWrapper parentMedia;
    @Nullable private final Torrent torrent;
    @Nullable private final SubtitleWrapper subtitle;

    @Nullable private String streamUrl;

    public StreamInfo(@NonNull final Torrent torrent, @NonNull final MediaWrapper media, @Nullable final MediaWrapper parentMedia,
            @Nullable final SubtitleWrapper subtitle) {
        this(torrent, media, parentMedia, null, subtitle);
    }

    public StreamInfo(@NonNull final String streamUrl, @NonNull final MediaWrapper media, @Nullable final MediaWrapper parentMedia) {
        this(null, media, parentMedia, streamUrl, null);
    }

    private StreamInfo(@Nullable final Torrent torrent, @NonNull final MediaWrapper media, @Nullable final MediaWrapper parentMedia,
            @Nullable final String streamUrl, @Nullable final SubtitleWrapper subtitle) {
        this.torrent = torrent;
        this.media = media;
        this.parentMedia = parentMedia;
        this.streamUrl = streamUrl;
        this.subtitle = subtitle;
    }

    private StreamInfo(Parcel in) {
        this.media = Parcels.unwrap(in.readParcelable(Media.class.getClassLoader()));
        this.parentMedia = Parcels.unwrap(in.readParcelable(Media.class.getClassLoader()));
        this.torrent = Parcels.unwrap(in.readParcelable(Torrent.class.getClassLoader()));
        this.subtitle = in.readParcelable(Subtitle.class.getClassLoader());

        this.streamUrl = in.readString();
    }

    @NonNull public String getFullTitle() {
        if (parentMedia != null && parentMedia.isShow()) {
            return String.format(Locale.US, "%s: %s", getParentMediaTitle(), getMediaTitle());
        } else {
            return getMediaTitle();
        }
    }

    @NonNull public MediaWrapper getMedia() {
        return media;
    }

    public boolean hasSubtitles() {
        return subtitle != null;
    }

    @Nullable public SubtitleWrapper getSubtitle() {
        return subtitle;
    }

    @NonNull public String getMediaTitle() {
        return media.getMedia().getTitle();
    }

    @Nullable public String getParentMediaTitle() {
        if (parentMedia != null) {
            return parentMedia.getMedia().getTitle();
        } else {
            return null;
        }
    }

    @Nullable public String getTorrentUrl() {
        if (torrent != null) {
            return torrent.getUrl();
        } else {
            return null;
        }
    }

    @Nullable public String getStreamUrl() {
        return streamUrl;
    }

    public void setStreamUrl(final String url) {
        streamUrl = url;
    }

    public int getPaletteColor() {
        if (media.hasColor()) {
            return media.getColor();
        } else if (parentMedia != null) {
            return parentMedia.getColor();
        } else {
            return MediaMeta.COLOR_NONE;
        }
    }

    public boolean hasParentMedia() {
        return parentMedia != null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(Parcels.wrap(this.media), 0);
        dest.writeParcelable(Parcels.wrap(this.parentMedia), 0);
        dest.writeParcelable(Parcels.wrap(this.torrent), 0);
        dest.writeParcelable(subtitle, 0);

        dest.writeString(streamUrl);
    }

    @Nullable public String getBackdropImage() {
        Media media = this.media.getMedia();
        if (media.getBackdrop() != null) {
            return media.getBackdrop();
        } else if (parentMedia != null) {
            return parentMedia.getMedia().getBackdrop();
        } else {
            return null;
        }
    }

    @Nullable public String getPosterImage() {
        Media media = this.media.getMedia();
        if (media.getPoster() != null) {
            return media.getPoster();
        } else if (parentMedia != null) {
            return this.media.getMedia().getPoster();
        } else {
            return null;
        }
    }

    public static final Creator<StreamInfo> CREATOR = new Creator<StreamInfo>() {
        public StreamInfo createFromParcel(Parcel source) {
            return new StreamInfo(source);
        }

        public StreamInfo[] newArray(int size) {
            return new StreamInfo[size];
        }
    };

}

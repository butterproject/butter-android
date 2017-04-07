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

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Show;

public class StreamInfo implements Parcelable {

    private String subtitleLanguage;
    private String quality;
    private String torrentUrl;
    private String videoLocation;
    private String title;
    private String imageUrl;
    private String headerImageUrl;

    private String showTitle;
    private String showEpisodeTitle;

    private Boolean isShow = false;
    private int color = Color.TRANSPARENT;
    private Media media;

    public StreamInfo(String torrentUrl) {
        this(null, null, torrentUrl, null, null);
    }

    public StreamInfo(Media media, String torrentUrl, String subtitleLanguage, String quality) {
        this(media, null, torrentUrl, subtitleLanguage, quality);
    }

    public StreamInfo(Media media, Show show, String torrentUrl, String subtitleLanguage, String quality) {
        this(media, show, torrentUrl, subtitleLanguage, quality, null);
    }

    public StreamInfo(Media media, Show show, String torrentUrl, String subtitleLanguage, String quality, String videoLocation) {
        this.torrentUrl = torrentUrl;
        this.subtitleLanguage = subtitleLanguage;
        this.quality = quality;
        this.videoLocation = videoLocation;

        if (media != null) {
            if (show != null) {
                title = show.title == null ? "" : show.title;
                title += media.title == null ? "" : ": " + media.title;
                imageUrl = show.image;
                headerImageUrl = show.headerImage;
                color = show.color;
                showTitle = show.title == null ? "" : show.title;
                showEpisodeTitle = media.title == null ? "" : media.title;
            } else {
                title = media.title == null ? "" : media.title;
                imageUrl = media.image;
                headerImageUrl = media.headerImage;
                color = media.color;
            }

            isShow = show != null;

            this.media = media;
        }
    }

    public boolean isShow() {
        return isShow;
    }

    public String getTitle() {
        return title;
    }

    public String getShowTitle() {
        return showTitle;
    }

    public String getShowEpisodeTitle() {
        return showEpisodeTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getHeaderImageUrl() {
        return headerImageUrl;
    }

    public String getSubtitleLanguage() {
        return subtitleLanguage;
    }

    public String getQuality() {
        return quality;
    }

    public String getTorrentUrl() {
        return torrentUrl;
    }

    public String getVideoLocation() {
        return videoLocation;
    }

    public int getPaletteColor() {
        return color;
    }

    public Media getMedia() {
        return media;
    }

    public void setSubtitleLanguage(String subtitleLanguage) {
        this.subtitleLanguage = subtitleLanguage;
    }

    public void setVideoLocation(String videoLocation) {
        this.videoLocation = videoLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.subtitleLanguage);
        dest.writeString(this.quality);
        dest.writeString(this.torrentUrl);
        dest.writeString(this.videoLocation);
        dest.writeString(this.imageUrl);
        dest.writeString(this.headerImageUrl);
        dest.writeString(this.title);
        dest.writeInt(this.isShow ? 1 : 0);
        dest.writeInt(this.color);
        dest.writeParcelable(this.media, 0);
        dest.writeString(this.showTitle);
        dest.writeString(this.showEpisodeTitle);
    }

    private StreamInfo(Parcel in) {
        this.subtitleLanguage = in.readString();
        this.quality = in.readString();
        this.torrentUrl = in.readString();
        this.videoLocation = in.readString();
        this.imageUrl = in.readString();
        this.headerImageUrl = in.readString();
        this.title = in.readString();
        this.isShow = in.readInt() == 1;
        this.color = in.readInt();
        this.media = in.readParcelable(Media.class.getClassLoader());
        this.showTitle = in.readString();
        this.showEpisodeTitle = in.readString();
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

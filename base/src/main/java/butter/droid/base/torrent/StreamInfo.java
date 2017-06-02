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
import android.support.annotation.NonNull;
import butter.droid.provider.base.Media;
import butter.droid.provider.base.Movie;
import butter.droid.provider.base.Streamable;
import org.parceler.Parcels;

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

    private int color = Color.TRANSPARENT;
    private Streamable streamable;

    public StreamInfo(String torrentUrl) {
        this(null, null, torrentUrl, null, null);
    }

    public StreamInfo(@NonNull Movie movie, String torrentUrl, String subtitleLanguage, String quality) {
        this(movie, movie, torrentUrl, subtitleLanguage, quality);
    }

    public StreamInfo(@NonNull Streamable streamable, @NonNull Media media, String torrentUrl, String subtitleLanguage, String quality) {
        this(streamable, media, torrentUrl, subtitleLanguage, quality, null);
    }

    public StreamInfo(@NonNull Streamable streamable, @NonNull Media media, String torrentUrl, String subtitleLanguage, String quality, String videoLocation) {
        this.torrentUrl = torrentUrl;
        this.subtitleLanguage = subtitleLanguage;
        this.quality = quality;
        this.videoLocation = videoLocation;
        this.streamable = streamable;

        if (media != null) { // TODO if media is show or movie
            title = media.getTitle() + ": " + streamable.getTitle();
            showTitle = media.getTitle();
            showEpisodeTitle = streamable.getTitle();
        } else {
            title = media.getTitle();
        }

        imageUrl = media.getPoster();
        headerImageUrl = media.getBackdrop();
        // color = media.color;
    }

    private StreamInfo(Parcel in) {
        this.subtitleLanguage = in.readString();
        this.quality = in.readString();
        this.torrentUrl = in.readString();
        this.videoLocation = in.readString();
        this.imageUrl = in.readString();
        this.headerImageUrl = in.readString();
        this.title = in.readString();
        this.color = in.readInt();
        this.streamable = Parcels.unwrap(in.readParcelable(Streamable.class.getClassLoader()));
        this.showTitle = in.readString();
        this.showEpisodeTitle = in.readString();
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

    public Streamable getStreamable() {
        return streamable;
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
        dest.writeInt(this.color);
        dest.writeParcelable(Parcels.wrap(this.streamable), 0);
        dest.writeString(this.showTitle);
        dest.writeString(this.showEpisodeTitle);
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

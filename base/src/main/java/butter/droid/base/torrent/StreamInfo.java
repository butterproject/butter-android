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

import android.os.Parcel;
import android.os.Parcelable;

import butter.droid.base.providers.media.models.Media;
import butter.droid.base.providers.media.models.Show;

public class StreamInfo implements Parcelable {

    private String mSubtitleLanguage;
    private String mQuality;
    private String mTorrentUrl;
    private String mTorrentFile;
    private String mVideoLocation;
    private String mTitle;
    private String mImageUrl;
    private String mHeaderImageUrl;

    private String mShowTitle;
    private String mShowEpisodeTitle;

    private Boolean mIsShow = false;
    private Integer mColor = -1;
    private Media mMedia;

    public StreamInfo(String torrentUrl) {
        this(null, null, torrentUrl, "", null, null);
    }

    public StreamInfo(Media media, String torrentUrl, String subtitleLanguage, String quality) {
        this(media, null, torrentUrl, "", subtitleLanguage, quality);
    }

    public StreamInfo(Media media, Show show, String torrentUrl, String torrentFile, String subtitleLanguage, String quality) {
        this(media, show, torrentUrl, torrentFile, subtitleLanguage, quality, null);
    }

    public StreamInfo(Media media, Show show, String torrentUrl, String torrentFile, String subtitleLanguage, String quality, String videoLocation) {
        mTorrentUrl = torrentUrl;
        mTorrentFile = torrentFile;
        mSubtitleLanguage = subtitleLanguage;
        mQuality = quality;
        mVideoLocation = videoLocation;

        if (media != null) {
            if (show != null) {
                mTitle = show.title == null ? "" : show.title;
                mTitle += media.title == null ? "" : ": " + media.title;
                mImageUrl = show.image;
                mHeaderImageUrl = show.headerImage;
                mColor = show.color;
                mShowTitle = show.title == null ? "" : show.title;
                mShowEpisodeTitle = media.title == null ? "" : media.title;
            } else {
                mTitle = media.title == null ? "" : media.title;
                mImageUrl = media.image;
                mHeaderImageUrl = media.headerImage;
                mColor = media.color;
            }

            mIsShow = show != null;

            mMedia = media;
        }
    }

    public boolean isShow() {
        return mIsShow;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getShowTitle() {
        return mShowTitle;
    }

    public String getShowEpisodeTitle() {
        return mShowEpisodeTitle;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getHeaderImageUrl() {
        return mHeaderImageUrl;
    }

    public String getSubtitleLanguage() {
        return mSubtitleLanguage;
    }

    public String getQuality() {
        return mQuality;
    }

    public String getTorrentUrl() {
        return mTorrentUrl;
    }

    public String getTorrentFile() {
        return mTorrentFile;
    }

    public String getVideoLocation() {
        return mVideoLocation;
    }

    public Integer getPaletteColor() {
        return mColor;
    }

    public Media getMedia() {
        return mMedia;
    }

    public void setSubtitleLanguage(String subtitleLanguage) {
        mSubtitleLanguage = subtitleLanguage;
    }

    public void setVideoLocation(String videoLocation) {
        mVideoLocation = videoLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mSubtitleLanguage);
        dest.writeString(this.mQuality);
        dest.writeString(this.mTorrentUrl);
        dest.writeString(this.mTorrentFile);
        dest.writeString(this.mVideoLocation);
        dest.writeString(this.mImageUrl);
        dest.writeString(this.mHeaderImageUrl);
        dest.writeString(this.mTitle);
        dest.writeInt(this.mIsShow ? 1 : 0);
        dest.writeInt(this.mColor);
        dest.writeParcelable(this.mMedia, 0);
        dest.writeString(this.mShowTitle);
        dest.writeString(this.mShowEpisodeTitle);
    }

    private StreamInfo(Parcel in) {
        this.mSubtitleLanguage = in.readString();
        this.mQuality = in.readString();
        this.mTorrentUrl = in.readString();
        this.mTorrentFile = in.readString();
        this.mVideoLocation = in.readString();
        this.mImageUrl = in.readString();
        this.mHeaderImageUrl = in.readString();
        this.mTitle = in.readString();
        this.mIsShow = in.readInt() == 1;
        this.mColor = in.readInt();
        this.mMedia = in.readParcelable(Media.class.getClassLoader());
        this.mShowTitle = in.readString();
        this.mShowEpisodeTitle = in.readString();
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
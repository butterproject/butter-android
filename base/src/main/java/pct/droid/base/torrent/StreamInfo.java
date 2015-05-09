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

package pct.droid.base.torrent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pct.droid.base.R;
import pct.droid.base.providers.media.models.Media;
import pct.droid.base.providers.media.models.Show;

public class StreamInfo implements Parcelable {
    private Media mMedia;
    private String mSubtitleLanguage;
    private String mQuality;
    private String mTorrentUrl;
    private String mVideoLocation;
    private Show mShow;

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
        mMedia = media;
        mShow = show;
        mTorrentUrl = torrentUrl;
        mSubtitleLanguage = subtitleLanguage;
        mQuality = quality;
        mVideoLocation = videoLocation;
    }

    public boolean isShow() {
        return null != mShow;
    }

    public Show getShow() {
        return mShow;
    }

    public Media getMedia() {
        return mMedia;
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

    public String getVideoLocation() {
        return mVideoLocation;
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
        dest.writeParcelable(this.mMedia, 0);
        dest.writeString(this.mSubtitleLanguage);
        dest.writeString(this.mQuality);
        dest.writeString(this.mTorrentUrl);
        dest.writeString(this.mVideoLocation);
        dest.writeParcelable(this.mShow, 0);
    }

    private StreamInfo(Parcel in) {
        this.mMedia = in.readParcelable(Media.class.getClassLoader());
        this.mSubtitleLanguage = in.readString();
        this.mQuality = in.readString();
        this.mTorrentUrl = in.readString();
        this.mVideoLocation = in.readString();
        this.mShow = in.readParcelable(Show.class.getClassLoader());
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